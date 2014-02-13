package org.inria.myriads.snoozenode.monitoring.comunicator.api.impl;

import java.io.IOException;

import org.inria.myriads.snoozecommon.communication.NetworkAddress;
import org.inria.myriads.snoozenode.configurator.database.DatabaseSettings;
import org.inria.myriads.snoozenode.monitoring.comunicator.api.MonitoringCommunicator;
import org.inria.myriads.snoozenode.monitoring.datasender.api.DataSender;
import org.inria.myriads.snoozenode.monitoring.datasender.api.impl.CassandraVirtualMachineDataSender;
import org.inria.myriads.snoozenode.monitoring.datasender.api.impl.TCPDataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Virtual Machine Cassandra Communicator.
 * 
 * @author msimonin
 *
 */
public class LocalControllerCassandraCommunicator implements MonitoringCommunicator
{
    /** Define the logger. */
    private static final Logger log_ = LoggerFactory.getLogger(GroupManagerCassandraCommunicator.class);
    
    /** Heartbeat sender.*/
    private DataSender heartbeatSender_;
    
    /** Monitoring sender.*/
    private DataSender monitoringSender_;

    /** Anomaly sender.*/
    private DataSender anomalySender_;
    
    /**
     * 
     * Virtual Machine Cassandra Communicator.
     * 
     * @param groupLeaderAddress        Group leader address.
     * @param databaseSettings          Database settings.
     * @throws IOException              Exception
     */
    public LocalControllerCassandraCommunicator(NetworkAddress groupLeaderAddress, DatabaseSettings databaseSettings)
            throws IOException
    {
        heartbeatSender_ = new TCPDataSender(groupLeaderAddress);
        anomalySender_ = heartbeatSender_;
        monitoringSender_ = new CassandraVirtualMachineDataSender(databaseSettings);
        log_.debug("GroupManagerCassandraCommunicator initialized");
    }
    
    @Override
    public void sendRegularData(Object data) throws IOException
    {
        log_.debug("Sending regular data");
        monitoringSender_.send(data);
    }

    @Override
    public void sendHeartbeatData(Object data) throws IOException
    {
        log_.debug("Sending heartbeat data");
        heartbeatSender_.send(data);
    }
    
    @Override
    public void sendAnomalyData(Object data) throws IOException
    {
        log_.debug("Sending anomaly data");
        anomalySender_.send(data);
    }

    @Override
    public void close()
    {
        log_.debug("Closing the communicator");
        monitoringSender_.close();
        heartbeatSender_.close();

    }



}