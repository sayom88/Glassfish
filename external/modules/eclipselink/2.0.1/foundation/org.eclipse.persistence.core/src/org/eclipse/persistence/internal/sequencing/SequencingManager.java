/*******************************************************************************
 * Copyright (c) 1998, 2010 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.sequencing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.persistence.sequencing.*;
import org.eclipse.persistence.sessions.Login;
import org.eclipse.persistence.sessions.server.*;
import org.eclipse.persistence.internal.databaseaccess.*;
import org.eclipse.persistence.internal.helper.ConcurrencyManager;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.DatabaseSessionImpl;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.logging.SessionLog;

/**
 * SequencingManager is private to EclipseLink.
 * It provides most of sequencing functionality.
 * It's accessed by DatabaseSession through getSequencingHome() method.
 *
 * Here's the lifecycle of SequencingManager.
 * InitialState: SequencingManager doesn't exist.
 *   Action: SequencingManager created -> Not connected State.
 * State: Not connected.
 *        isConnected() returns false;
 *        getSequencingControl() could be used;
 *        getSequencing() == getSequencingServer() == getSequencingCallbackFactory() == null;
 *   Action: onConnect is called -> Connected State.
 * State: Connected.
 *        isConnected() returns true;
 *        getSequencingControl() could be used;
 *        getSequencing() could be used;
 *        in case ownwerSession is a ServerSession getSequencingServer() could be used;
 *   Action: onDisconnect is called -> Not connected State.
 *
 * Here's a sketch of SequencingManager architecture.
 * The main 4 objects comprising SessionManager are:
 *      valueGenarationPolicy;
 *      preallocationHandler;
 *      connectionHandler;
 *      state;
 *
 * That's how they evolve during lifetime of SequencingManager object:
 * Not connected State:
 *      preallocationHandler doesn't have any preallocated sequencing values.
 *      connectionHandler == null;
 *      state == null;
 *
 * Connected State:
 *      preallocationHandler may contain preallocated sequencing values.
 *      valueGenarationPolicy != null;
 *      state != null;
 *
 * The most important method of the class is onConnect():
 * that's where, using values of the attributes'(accessible through SequencingControl):
 *      shouldUseSeparateConnection;
 *      login;
 *      minPoolSize;
 *      maxPoolSize;
 * as well as boolean flags returned by valueGenerationPolicy methods:
 *      shouldAcquireValueAfterInsert();
 *      shouldUsePreallocation();
 *      shouldUseSeparateConnection();
 *      shouldUseTransaction();
 * one of implementors of inner interface State is created.
 *
 * Once in Connected State, neither changes to attributes, nor to returns of valueGenerationPolicy's
 * four should... methods can change the state object.
 * To change the state object, onDisconnect(), than onConnect() should be called.
 * There is no need to do it directly: each of the following methods
 * available through SequencingControl does that:
 *      setValueGenerationPolicy;
 *      setShouldUseNativeSequencing;
 *      setShouldUseTableSequencing;
 *      resetSequencing;
 */
class SequencingManager implements SequencingHome, SequencingServer, SequencingControl {
    private DatabaseSessionImpl ownerSession;
    private SequencingConnectionHandler connectionHandler;
    private PreallocationHandler preallocationHandler;
    private int whenShouldAcquireValueForAll;
    private Vector connectedSequences;
    boolean atLeastOneSequenceShouldUseTransaction;
    boolean atLeastOneSequenceShouldUsePreallocation;

    // state ids
    private static final int NOPREALLOCATION = 0;
    private static final int PREALLOCATION_NOTRANSACTION = 1;
    private static final int PREALLOCATION_TRANSACTION_NOACCESSOR = 2;
    private static final int PREALLOCATION_TRANSACTION_ACCESSOR = 3;
    private static final int NUMBER_OF_STATES = 4;
    private State[] states;
    private Map<String, ConcurrencyManager> locks;
    private SequencingCallbackFactory callbackFactory;
    private SequencingServer server;
    private Sequencing seq;
    private boolean shouldUseSeparateConnection;
    private Login login;
    private int minPoolSize = -1;
    private int maxPoolSize = -1;
    private int initialPoolSize = -1;
    private ConnectionPool connectionPool;

    public SequencingManager(DatabaseSessionImpl ownerSession) {
        this.ownerSession = ownerSession;
    }

    protected DatabaseSessionImpl getOwnerSession() {
        return ownerSession;
    }

    protected void createConnectionHandler() {
        boolean isServerSession = getOwnerSession().isServerSession();

        if (getLogin() == null) {
            Login login;
            if (isServerSession) {
                login = ((ServerSession)getOwnerSession()).getReadConnectionPool().getLogin();
            } else {
                login = getOwnerSession().getDatasourceLogin();
            }
            setLogin((Login)login.clone());
        }

        if (getLogin() != null) {
            if (getLogin().shouldUseExternalTransactionController()) {
                throw ValidationException.invalidSequencingLogin();
            }
        }

        if (isServerSession) {
            ConnectionPool pool = null;
            if (this.connectionPool == null) {
                if (getLogin().shouldUseExternalConnectionPooling()) {
                    pool = new ExternalConnectionPool("sequencing", getLogin(), (ServerSession)getOwnerSession());
                } else {
                    if (getMinPoolSize() == -1) {
                        setMinPoolSize(2);
                    }
                    if (getMaxPoolSize() == -1) {
                        setMinPoolSize(2);
                    }
                    if (getInitialPoolSize() == -1) {
                        setInitialPoolSize(1);
                    }
                    pool = new ConnectionPool("sequencing", getLogin(), getInitialPoolSize(), getMinPoolSize(), getMaxPoolSize(), (ServerSession)getOwnerSession());
                }
            } else {
                pool = this.connectionPool;
            }

            setConnectionHandler(new ServerSessionConnectionHandler(pool));

        } else {
            setConnectionHandler(new DatabaseSessionConnectionHandler(getOwnerSession(), getLogin()));
        }
    }

    public SequencingControl getSequencingControl() {
        return this;
    }

    protected void setSequencing(Sequencing sequencing) {
        this.seq = sequencing;
    }

    public Sequencing getSequencing() {
        return seq;
    }

    protected void setSequencingServer(SequencingServer server) {
        this.server = server;
    }

    public SequencingServer getSequencingServer() {
        return server;
    }

    protected void setSequencingCallbackFactory(SequencingCallbackFactory callbackFactory) {
        this.callbackFactory = callbackFactory;
    }

    public boolean isSequencingCallbackRequired() {
        return this.callbackFactory != null;
    }

    public boolean shouldUseSeparateConnection() {
        return shouldUseSeparateConnection;
    }

    public void setShouldUseSeparateConnection(boolean shouldUseSeparateConnection) {
        this.shouldUseSeparateConnection = shouldUseSeparateConnection;
    }

    public boolean isConnectedUsingSeparateConnection() {
        return isConnected() && (getConnectionHandler() != null);
    }

    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int size) {
        this.minPoolSize = size;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int size) {
        this.maxPoolSize = size;
    }

    public int getInitialPoolSize() {
        return this.initialPoolSize;
    }

    public void setInitialPoolSize(int size) {
        this.initialPoolSize = size;
    }

    public boolean isConnected() {
        return states != null;
    }

    // SequencingSetup
    protected SequencingConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    protected void setConnectionHandler(SequencingConnectionHandler handler) {
        this.connectionHandler = handler;
    }

    public ConnectionPool getConnectionPool() {
        if ((getConnectionHandler() != null) && (getConnectionHandler() instanceof ServerSessionConnectionHandler)) {
            return ((ServerSessionConnectionHandler)getConnectionHandler()).getPool();
        }
        return this.connectionPool;
    }
    
    public Object getNextValue(Class cls) {
        return getNextValue(getOwnerSession(), cls);
    }

    public void initializePreallocated() {
        if (getPreallocationHandler() != null) {
            getPreallocationHandler().initializePreallocated();
        }
    }

    public void initializePreallocated(String seqName) {
        if (getPreallocationHandler() != null) {
            getPreallocationHandler().initializePreallocated(seqName);
        }
    }

    protected void setLocks(Map locks) {
        this.locks = locks;
    }

    protected Map<String, ConcurrencyManager> getLocks() {
        return locks;
    }

    /**
     * Acquire a lock for the sequence name.
     * A lock should be, and only be, acquired when allocating new sequences from the database.
     */
    protected void acquireLock(String sequenceName) {
        ConcurrencyManager manager = getLocks().get(sequenceName);
        if (manager == null) {
            synchronized (getLocks()) {
                manager = getLocks().get(sequenceName);
                if (manager == null) {
                    manager = new ConcurrencyManager();
                    getLocks().put(sequenceName, manager);
                }
            }
        }
        manager.acquire();
    }
    
    /**
     * Release a lock for the sequence name.
     */
    protected void releaseLock(String seqName) {
        ConcurrencyManager manager = getLocks().get(seqName);
        manager.release();
    }

    protected Sequence getSequence(Class cls) {
        //** should check here that sequencing is used?
        String seqName = getOwnerSession().getDescriptor(cls).getSequenceNumberName();
        return getSequence(seqName);
    }

    protected void logDebugPreallocation(String seqName, Object firstSequenceValue, Vector sequences) {
        if (getOwnerSession().shouldLog(SessionLog.FINEST, SessionLog.SEQUENCING)) {
            // the first value has been already removed from sequences vector 
            Object[] args = { seqName, new Integer(sequences.size() + 1), firstSequenceValue, sequences.lastElement() };
            getOwnerSession().log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequencing_preallocation", args);
        }
    }

    protected void logDebugLocalPreallocation(AbstractSession writeSession, String seqName, Vector sequences, Accessor accessor) {
        if (writeSession.shouldLog(SessionLog.FINEST, SessionLog.SEQUENCING)) {
            Object[] args = { seqName, new Integer(sequences.size()), sequences.firstElement(), sequences.lastElement() };
            writeSession.log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequencing_localPreallocation", args, accessor);
        }
    }

    static abstract class State {
        abstract Object getNextValue(Sequence sequence, AbstractSession writeSession);

        SequencingCallbackFactory getSequencingCallbackFactory() {
            return null;
        }

        public String toString() {
            String name = getClass().getName();
            return name.substring(name.lastIndexOf('$') + 1);
        }
    }

    /**
     * Uses preallocation, uses transaction, no separate connection.
     * This is used for a DatabaseSession, or a ServerSession not using native sequencing,
     * and not using a sequence connection pool.
     * This is used by default for table sequencing, unless a sequence connection pool is specified,
     * however it should only be used if there is no non-JTA login available.
     * This will use the writeConnection, but use individual transactions per sequence allocation,
     * unless the unit of work is in an early transaction, or the connection is JTA (this may deadlock).
     */
    class Preallocation_Transaction_NoAccessor_State extends State implements SequencingCallbackFactory {

        public class SequencingCallbackImpl implements SequencingCallback {
            Map localSequences = new HashMap();
            
            /**
            * INTERNAL:
            * Called after transaction has committed (commit in non-jta case; after completion - jta case).
            * Should not be called after rollback.
            */
            public void afterCommit(Accessor accessor) {
                afterCommitInternal(localSequences, accessor);
            }
            
            public Map getPreallocatedSequenceValues() {
                return localSequences;
            }
        }
        
        SequencingCallbackFactory getSequencingCallbackFactory() {
            return this;
        }

        /**
        * INTERNAL:
        * Creates SequencingCallback.
        */
        public SequencingCallback createSequencingCallback() {
            return new SequencingCallbackImpl();
        }
        
        /**
         * Release any locally allocated sequence back to the global sequence pool.
         */
        void afterCommitInternal(Map localSequences, Accessor accessor) {
            Iterator it = localSequences.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                String seqName = (String)entry.getKey();
                Vector localSequenceForName = (Vector)entry.getValue();
                if (!localSequenceForName.isEmpty()) {
                    getPreallocationHandler().setPreallocated(seqName, localSequenceForName);
                    // clear all localSequencesForName
                    localSequenceForName.clear();
                }
            }
            if(accessor != null) {
                getOwnerSession().log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequencing_afterTransactionCommitted", null, accessor);
            } else {
                getOwnerSession().log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequencing_afterTransactionCommitted", null);
            }
        }
        
        SequencingCallbackImpl getCallbackImpl(AbstractSession writeSession, Accessor accessor) {
            SequencingCallbackImpl seqCallbackImpl;
            if(writeSession.hasExternalTransactionController()) {
                // note that controller obtained from writeSession (not from ownerSession) -
                // the difference is important in case of ownerSession being a member of SessionBroker:
                // in that case only writeSession (which is either ClientSession or DatabaseSession) always has
                // the correct controller.
                seqCallbackImpl = (SequencingCallbackImpl)writeSession.getExternalTransactionController().getActiveSequencingCallback(getOwnerSession(), getSequencingCallbackFactory());
            } else {
                seqCallbackImpl = (SequencingCallbackImpl)accessor.getSequencingCallback(getSequencingCallbackFactory());
            }
            return seqCallbackImpl;
        }

        /**
         * Return the next sequence value.
         * First check the global pool, if empty then allocate new sequences locally.
         */
        public Object getNextValue(Sequence sequence, AbstractSession writeSession) {
            String seqName = sequence.getName();
            if(sequence.getPreallocationSize() > 1) {
                Queue sequencesForName = getPreallocationHandler().getPreallocated(seqName);
                // First grab the first sequence value without locking, a lock is only required if empty.
                Object sequenceValue = sequencesForName.poll();
                if (sequenceValue != null) {
                    return sequenceValue;
                }
                // KeepLocked indicates whether the sequence lock should be kept for the whole duration of this method.
                // Of course the lock should be released in any case when the method returns or throws an exception.
                // This is only used if a sequence transaction was begun by the unit of work,
                // and will be committed before the unit of work commit.
                boolean keepLocked = false;
                if (!getOwnerSession().getDatasourceLogin().shouldUseExternalTransactionController() && !writeSession.isInTransaction()) {
                    // To prevent several threads from simultaneously allocating a separate bunch of
                    // sequencing numbers each. With keepLocked==true the first thread locks out others
                    // until it copies the obtained sequence numbers to the global storage.
                    // Note that this optimization possible only in non-jts case when there is no transaction.
                    acquireLock(seqName);
                    try {
                        sequenceValue = sequencesForName.poll();
                        if (sequenceValue != null) {
                            return sequenceValue;
                        }
                        writeSession.beginTransaction();//write accessor is set in begin
                        keepLocked = true;
                    } finally {
                        if (!keepLocked) {
                            releaseLock(seqName);
                        }
                    }
                }
    
                Accessor accessor;
                Vector localSequencesForName;
                if (!keepLocked) {
                    writeSession.beginTransaction();//write accessor is set in begin
                }
                try {
                    accessor = writeSession.getAccessor();
                    SequencingCallbackImpl seqCallbackImpl = getCallbackImpl(writeSession, accessor);
                    Map localSequences = seqCallbackImpl.getPreallocatedSequenceValues();
                    localSequencesForName = (Vector)localSequences.get(seqName);
                    if ((localSequencesForName == null) || localSequencesForName.isEmpty()) {
                        localSequencesForName = sequence.getGeneratedVector(null, writeSession);
                        localSequences.put(seqName, localSequencesForName);
                        logDebugLocalPreallocation(writeSession, seqName, localSequencesForName, accessor);
                    }
                } catch (RuntimeException ex) {
                    if (keepLocked) {
                        releaseLock(seqName);
                    }
                    try {
                        // make sure to rollback the transaction we've begun
                        writeSession.rollbackTransaction();
                    } catch (Exception rollbackException) {
                        // ignore rollback exception
                    }
    
                    // don't eat the original exception
                    throw ex;
                }
    
                try {
                    try {
                        // commitTransaction may copy preallocated sequence numbers 
                        // from localSequences to preallocationHandler: that happens
                        // if it isn't a nested transaction, and sequencingCallback.afterCommit 
                        // method has been called.
                        // In this case:
                        // 1. localSequences corresponding to the accessor
                        //    has been removed from accessorToPreallocated;
                        // 2. All its members are empty (therefore localSequenceForName is empty).
                        writeSession.commitTransaction();
                    } catch (DatabaseException ex) {
                        try {
                            // make sure to rollback the transaction we've begun
                            writeSession.rollbackTransaction();
                        } catch (Exception rollbackException) {
                            // ignore rollback exception
                        }
                        // don't eat the original exception
                        throw ex;
                    }
    
                    if (!localSequencesForName.isEmpty()) {
                        // localSeqencesForName is not empty, that means
                        // sequencingCallback has not been called.
                        sequenceValue = localSequencesForName.remove(0);
                        return sequenceValue;
                    } else {
                        // localSeqencesForName is empty, that means
                        // sequencingCallback has been called.
                        sequenceValue = sequencesForName.poll();
                        if (sequenceValue != null) {
                            return sequenceValue;
                        }
                        return getNextValue(sequence, writeSession);
                    }
                } finally {
                    if(keepLocked) {
                        releaseLock(seqName);
                    }
                }
            } else {
                writeSession.beginTransaction();
                try {
                    // preallocation size is 1 - just return the first (and only) element of the allocated vector.
                    Object sequenceValue = sequence.getGeneratedVector(null, writeSession).firstElement();
                    writeSession.commitTransaction();
                    return sequenceValue;
                } catch (RuntimeException ex) {
                    try {
                        // make sure to rollback the transaction we've begun
                        writeSession.rollbackTransaction();
                    } catch (Exception rollbackException) {
                        // ignore rollback exception
                    }
                    
                    // don't eat the original exception
                    throw ex;
                }
            }
        }
    }

    /**
     * Uses preallocation, uses transaction, and acquires an accessor.
     * This is used in a ServerSession with a sequence connection pool.
     * This is typically the default behavior.
     */
    class Preallocation_Transaction_Accessor_State extends State {
        public Object getNextValue(Sequence sequence, AbstractSession writeSession) {
            String seqName = sequence.getName();
            if(sequence.getPreallocationSize() > 1) {
                Queue sequencesForName = getPreallocationHandler().getPreallocated(seqName);
                // First try to get the next sequence value without locking.
                Object sequenceValue = sequencesForName.poll();
                if (sequenceValue != null) {
                    return sequenceValue;
                }
                // Sequences are empty, so must lock and allocate next batch of sequences.
                acquireLock(seqName);
                try {
                    sequenceValue = sequencesForName.poll();
                    if (sequenceValue != null) {
                        return sequenceValue;
                    }
                    // note that accessor.getLogin().shouldUseExternalTransactionController()
                    // should be set to false
                    Accessor accessor = getConnectionHandler().acquireAccessor();
                    try {
                        accessor.beginTransaction(writeSession);
                        try {
                            Vector sequences = sequence.getGeneratedVector(accessor, writeSession);
                            accessor.commitTransaction(writeSession);
                            // Remove the first value before adding to the global cache to ensure this thread gets one.
                            sequenceValue = sequences.remove(0);
                            // copy remaining values to global cache.
                            getPreallocationHandler().setPreallocated(seqName, sequences);
                            logDebugPreallocation(seqName, sequenceValue, sequences);
                        } catch (RuntimeException ex) {
                            try {
                                // make sure to rollback the transaction we've begun
                                accessor.rollbackTransaction(writeSession);
                            } catch (Exception rollbackException) {
                                // ignore rollback exception
                            }
                            // don't eat the original exception
                            throw ex;
                        }
                    } finally {
                        getConnectionHandler().releaseAccessor(accessor);
                    }
                } finally {
                    releaseLock(seqName);
                }
                return sequenceValue;
            } else {
                // note that accessor.getLogin().shouldUseExternalTransactionController()
                // should be set to false
                Accessor accessor = getConnectionHandler().acquireAccessor();
                try {
                    accessor.beginTransaction(writeSession);
                    try {
                        // preallocation size is 1 - just return the first (and only) element of the allocated vector.
                        Object sequenceValue = sequence.getGeneratedVector(accessor, writeSession).firstElement();
                        accessor.commitTransaction(writeSession);
                        return sequenceValue;
                    } catch (RuntimeException ex) {
                        try {
                            // make sure to rollback the transaction we've begun
                            accessor.rollbackTransaction(writeSession);
                        } catch (Exception rollbackException) {
                            // ignore rollback exception
                        }
                        // don't eat the original exception
                        throw ex;
                    }
                } finally {
                    getConnectionHandler().releaseAccessor(accessor);
                }
            }
        }
    }

    /**
     * Using preallocation, NoTransaction, NoAccessor.
     * This is used by native sequence objects.
     * No transaction is required as sequence objects are non-transactional.
     */
    class Preallocation_NoTransaction_State extends State {
        public Object getNextValue(Sequence sequence, AbstractSession writeSession) {
            String seqName = sequence.getName();
            if(sequence.getPreallocationSize() > 1) {
                Queue sequencesForName = getPreallocationHandler().getPreallocated(seqName);
                // First try to get the next sequence value without locking.
                Object sequenceValue = sequencesForName.poll();
                if (sequenceValue != null) {
                    return sequenceValue;
                }
                // Sequences are empty, so must lock and allocate next batch of sequences.
                acquireLock(seqName);
                try {
                    sequenceValue = sequencesForName.poll();
                    if (sequenceValue != null) {
                        return sequenceValue;
                    }
                    Vector sequences = sequence.getGeneratedVector(null, writeSession);
                    // Remove the first value before adding to the global cache to ensure this thread gets one.
                    sequenceValue = sequences.remove(0);
                    // copy remaining values to global cache.
                    getPreallocationHandler().setPreallocated(seqName, sequences);
                    logDebugPreallocation(seqName, sequenceValue, sequences);
                } finally {
                    releaseLock(seqName);
                }
                return sequenceValue;
            } else {
                // preallocation size is 1 - just return the first (and only) element of the allocated vector.
                return sequence.getGeneratedVector(null, writeSession).firstElement();
            }
        }
    }

    /**
     * Using NoPreallocation, no transaction, no Accessor.
     * This is only used for identity sequencing when preallocation is not possible.
     * The writeSession is always in a transaction, so a transaction is never required.
     * Table or sequence object with preallocation size 1 still goes through the preallocation state.
     */
    class NoPreallocation_State extends State {
        public Object getNextValue(Sequence sequence, AbstractSession writeSession) {
            return sequence.getGeneratedValue(null, writeSession);
        }
    }

    public void resetSequencing() {
        if (isConnected()) {
            onDisconnect();
            onConnect();
        }
    }

    /**
     * Initialize the sequences on login.
     */
    public void onConnect() {
        if (isConnected()) {
            return;
        }

        if (!getOwnerSession().getProject().usesSequencing()) {
            return;
        }

        onConnectAllSequences();

        boolean onExceptionDisconnectPreallocationHandler = false;
        boolean onExceptionDisconnectConnectionHandler = false;

        try {
            if (!shouldUseSeparateConnection()) {
                setConnectionHandler(null);
            } else if (atLeastOneSequenceShouldUseTransaction) {
                if (getConnectionHandler() == null) {
                    createConnectionHandler();
                }
                if (getConnectionHandler() != null) {
                    getConnectionHandler().onConnect();
                    onExceptionDisconnectConnectionHandler = true;
                }
            }

            if (atLeastOneSequenceShouldUsePreallocation) {
                if (getPreallocationHandler() == null) {
                    createPreallocationHandler();
                }
                getPreallocationHandler().onConnect();
                onExceptionDisconnectPreallocationHandler = true;
            }

            initializeStates();

        } catch (RuntimeException ex) {
            onDisconnectAllSequences();
            if (getConnectionHandler() != null) {
                if (onExceptionDisconnectConnectionHandler) {
                    getConnectionHandler().onDisconnect();
                }
                setConnectionHandler(null);
            }
            if (getPreallocationHandler() != null) {
                if (onExceptionDisconnectPreallocationHandler) {
                    getPreallocationHandler().onDisconnect();
                }
                clearPreallocationHandler();
            }
            throw ex;
        }
        if (atLeastOneSequenceShouldUsePreallocation) {
            setLocks(new ConcurrentHashMap(20));
        }
        createSequencingCallbackFactory();
        if(getOwnerSession().hasExternalTransactionController()) {
            getOwnerSession().getExternalTransactionController().initializeSequencingListeners();
        }
        if (getOwnerSession().isServerSession()) {
            setSequencingServer(this);
        }
        setSequencing(this);
        logDebugSequencingConnected();
    }

    public void onDisconnect() {
        if (!isConnected()) {
            return;
        }

        setSequencing(null);
        setSequencingServer(null);
        setSequencingCallbackFactory(null);
        if(getOwnerSession().hasExternalTransactionController()) {
            getOwnerSession().getExternalTransactionController().clearSequencingListeners();
        }
        setLocks(null);
        clearStates();

        if (getConnectionHandler() != null) {
            getConnectionHandler().onDisconnect();
            setConnectionHandler(null);
        }
        if (getPreallocationHandler() != null) {
            getPreallocationHandler().onDisconnect();
            clearPreallocationHandler();
        }
        onDisconnectAllSequences();
        getOwnerSession().log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequencing_disconnected");
    }

    protected PreallocationHandler getPreallocationHandler() {
        return preallocationHandler;
    }

    protected void createPreallocationHandler() {
        preallocationHandler = new PreallocationHandler();
    }

    protected void clearPreallocationHandler() {
        preallocationHandler = null;
    }

    protected void onConnectAllSequences() {
        connectedSequences = new Vector();
        boolean shouldUseTransaction = false;
        boolean shouldUsePreallocation = false;
        boolean shouldAcquireValueAfterInsert = false;
        Iterator descriptors = getOwnerSession().getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            // Find root sequence, because inheritance needs to be resolved here.
            // TODO: The way we initialize sequencing needs to be in line with descriptor init.
            ClassDescriptor parentDescriptor = descriptor;
            while (!parentDescriptor.usesSequenceNumbers() && parentDescriptor.isChildDescriptor()) {
                ClassDescriptor newDescriptor = getOwnerSession().getDescriptor(parentDescriptor.getInheritancePolicy().getParentClass());
                // Avoid issue with error cases of self parent, or null parent.
                if ((newDescriptor == null) || (newDescriptor == parentDescriptor)) {
                    break;
                }
                parentDescriptor = newDescriptor;
            }
            if (!parentDescriptor.usesSequenceNumbers()) {
                continue;
            }
            String seqName = parentDescriptor.getSequenceNumberName();
            Sequence sequence = getSequence(seqName);
            if (sequence == null) {
                sequence = new DefaultSequence(seqName);
                getOwnerSession().getDatasourcePlatform().addSequence(sequence);
            }
            // PERF: Initialize the sequence, this avoid having to look it up every time.
            descriptor.setSequence(sequence);
            if (connectedSequences.contains(sequence)) {
                continue;
            }
            try {
                if (sequence instanceof DefaultSequence && !connectedSequences.contains(getDefaultSequence())) {
                    getDefaultSequence().onConnect(getOwnerSession().getDatasourcePlatform());
                    connectedSequences.add(0, getDefaultSequence());
                    shouldUseTransaction |= getDefaultSequence().shouldUseTransaction();
                    shouldUsePreallocation |= getDefaultSequence().shouldUsePreallocation();
                    shouldAcquireValueAfterInsert |= getDefaultSequence().shouldAcquireValueAfterInsert();
                }
                sequence.onConnect(getOwnerSession().getDatasourcePlatform());
                connectedSequences.addElement(sequence);
                shouldUseTransaction |= sequence.shouldUseTransaction();
                shouldUsePreallocation |= sequence.shouldUsePreallocation();
                shouldAcquireValueAfterInsert |= sequence.shouldAcquireValueAfterInsert();
            } catch (RuntimeException ex) {
                // defaultSequence has to disconnect the last
                for (int i = connectedSequences.size() - 1; i >= 0; i--) {
                    try {
                        Sequence sequenceToDisconnect = (Sequence)connectedSequences.elementAt(i);
                        sequenceToDisconnect.onDisconnect(getOwnerSession().getDatasourcePlatform());
                    } catch (RuntimeException ex2) {
                        //ignore
                    }
                }
                connectedSequences = null;
                throw ex;
            }
        }

        if (shouldAcquireValueAfterInsert && !shouldUsePreallocation) {
            whenShouldAcquireValueForAll = AFTER_INSERT;
        } else if (!shouldAcquireValueAfterInsert && shouldUsePreallocation) {
            whenShouldAcquireValueForAll = BEFORE_INSERT;
        }
        atLeastOneSequenceShouldUseTransaction = shouldUseTransaction;
        atLeastOneSequenceShouldUsePreallocation = shouldUsePreallocation;
    }

    protected void onDisconnectAllSequences() {
        RuntimeException exception = null;

        // defaultSequence has to disconnect the last
        for (int i = connectedSequences.size() - 1; i >= 0; i--) {
            try {
                Sequence sequenceToDisconnect = (Sequence)connectedSequences.elementAt(i);
                sequenceToDisconnect.onDisconnect(getOwnerSession().getDatasourcePlatform());
            } catch (RuntimeException ex) {
                if (exception == null) {
                    exception = ex;
                }
            }
        }
        connectedSequences = null;
        whenShouldAcquireValueForAll = UNDEFINED;
        atLeastOneSequenceShouldUseTransaction = false;
        atLeastOneSequenceShouldUsePreallocation = false;
        if (exception != null) {
            throw exception;
        }
    }

    protected void initializeStates() {
        states = new State[NUMBER_OF_STATES];

        Iterator itConnectedSequences = connectedSequences.iterator();
        while (itConnectedSequences.hasNext()) {
            Sequence sequence = (Sequence)itConnectedSequences.next();
            State state = getState(sequence.shouldUsePreallocation(), sequence.shouldUseTransaction());
            if (state == null) {
                createState(sequence.shouldUsePreallocation(), sequence.shouldUseTransaction());
            }
        }
    }

    protected void clearStates() {
        states = null;
    }

    protected int getStateId(boolean shouldUsePreallocation, boolean shouldUseTransaction) {
        if (!shouldUsePreallocation) {
            // Non-Oracle native sequencing uses this state
            return NOPREALLOCATION;
        } else if (!shouldUseTransaction) {
            // Oracle native sequencing uses this state
            return PREALLOCATION_NOTRANSACTION;
        } else if (getConnectionHandler() == null) {
            // TableSequence and UnaryTableSequence in case there is no separate connection(s) available use this state
            return PREALLOCATION_TRANSACTION_NOACCESSOR;
        } else/*if(getConnectionHandler()!=null)*/
         {
            // TableSequence and UnaryTableSequence in case there is separate connection(s) available use this state
            return PREALLOCATION_TRANSACTION_ACCESSOR;
        }
    }

    protected State getState(boolean shouldUsePreallocation, boolean shouldUseTransaction) {
        return states[getStateId(shouldUsePreallocation, shouldUseTransaction)];
    }

    protected void createState(boolean shouldUsePreallocation, boolean shouldUseTransaction) {
        if (!shouldUsePreallocation) {
            // Non-Oracle native sequencing uses this state
            states[NOPREALLOCATION] = new NoPreallocation_State();
        } else if (!shouldUseTransaction) {
            // Oracle native sequencing uses this state
            states[PREALLOCATION_NOTRANSACTION] = new Preallocation_NoTransaction_State();
        } else if (getConnectionHandler() == null) {
            // TableSequence and UnaryTableSequence in case there is no separate connection(s) available use this state
            states[PREALLOCATION_TRANSACTION_NOACCESSOR] = new Preallocation_Transaction_NoAccessor_State();
        } else/*if(getConnectionHandler()!=null)*/
         {
            // TableSequence and UnaryTableSequence in case there is separate connection(s) available use this state
            states[PREALLOCATION_TRANSACTION_ACCESSOR] = new Preallocation_Transaction_Accessor_State();
        }
    }

    protected void createSequencingCallbackFactory() {
        if (states[PREALLOCATION_TRANSACTION_NOACCESSOR] != null) {
            setSequencingCallbackFactory(states[PREALLOCATION_TRANSACTION_NOACCESSOR].getSequencingCallbackFactory());
        } else {
            setSequencingCallbackFactory(null);
        }
    }

    public Object getNextValue(AbstractSession writeSession, Class cls) {
        Sequence sequence = getSequence(cls);
        State state = getState(sequence.shouldUsePreallocation(), sequence.shouldUseTransaction());
        return state.getNextValue(sequence, writeSession);
    }
    
    protected void logDebugSequencingConnected() {
        Vector[] sequenceVectors = new Vector[NUMBER_OF_STATES];
        Iterator itConnectedSequences = connectedSequences.iterator();
        while (itConnectedSequences.hasNext()) {
            Sequence sequence = (Sequence)itConnectedSequences.next();
            int stateId = getStateId(sequence.shouldUsePreallocation(), sequence.shouldUseTransaction());
            Vector v = sequenceVectors[stateId];
            if (v == null) {
                v = new Vector();
                sequenceVectors[stateId] = v;
            }
            v.addElement(sequence);
        }
        for (int i = 0; i < NUMBER_OF_STATES; i++) {
            Vector v = sequenceVectors[i];
            if (v != null) {
                getOwnerSession().log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequencing_connected", states[i]);
                for (int j = 0; j < v.size(); j++) {
                    Sequence sequence = (Sequence)v.elementAt(j);
                    Object[] args = { sequence.getName(), Integer.toString(sequence.getPreallocationSize()),
                            Integer.toString(sequence.getInitialValue())};
                    getOwnerSession().log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequence_without_state", args);
                }
            }
        }
    }

    public int getPreallocationSize() {
        return getDefaultSequence().getPreallocationSize();
    }

    public int getInitialValue() {
        return getDefaultSequence().getInitialValue();
    }

    public int whenShouldAcquireValueForAll() {
        return whenShouldAcquireValueForAll;
    }

    protected Sequence getDefaultSequence() {
        return getOwnerSession().getDatasourcePlatform().getDefaultSequence();
    }

    protected Sequence getSequence(String seqName) {
        return getOwnerSession().getDatasourcePlatform().getSequence(seqName);
    }

    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }
}
