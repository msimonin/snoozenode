/**
 * Copyright (C) 2010-2013 Eugen Feller, INRIA <eugen.feller@inria.fr>
 *
 * This file is part of Snooze, a scalable, autonomic, and
 * energy-aware virtual machine (VM) management framework.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package org.inria.myriads.snoozenode.localcontroller.monitoring.consumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import org.inria.myriads.snoozecommon.communication.NetworkAddress;
import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerDescription;
import org.inria.myriads.snoozecommon.communication.localcontroller.MonitoringThresholds;
import org.inria.myriads.snoozecommon.guard.Guard;
import org.inria.myriads.snoozenode.configurator.database.DatabaseSettings;
import org.inria.myriads.snoozenode.database.api.LocalControllerRepository;
import org.inria.myriads.snoozenode.localcontroller.monitoring.listener.VirtualMachineMonitoringListener;
import org.inria.myriads.snoozenode.localcontroller.monitoring.service.InfrastructureMonitoring;
import org.inria.myriads.snoozenode.localcontroller.monitoring.threshold.ThresholdCrossingDetector;
import org.inria.myriads.snoozenode.localcontroller.monitoring.transport.AggregatedVirtualMachineData;
import org.inria.myriads.snoozenode.localcontroller.monitoring.transport.LocalControllerDataTransporter;
import org.inria.myriads.snoozenode.monitoring.comunicator.MonitoringCommunicatorFactory;
import org.inria.myriads.snoozenode.monitoring.comunicator.api.MonitoringCommunicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual machine monitor data consumer.
 * 
 * @author Eugen Feller
 */
public final class VirtualMachineMonitorDataConsumer 
    implements Runnable
{
    /** Define the logger. */
    private static final Logger log_ = LoggerFactory.getLogger(VirtualMachineMonitorDataConsumer.class);
  
    /** Data queue. */
    private BlockingQueue<AggregatedVirtualMachineData> dataQueue_;

    /** Virtual machine monitoring callback. */
    private VirtualMachineMonitoringListener callback_;
    
    /** Thhreshold crossing detector. */
    private ThresholdCrossingDetector crossingDetector_;
    
    /** Local controller identifier. */
    private String localControllerId_;
    
    /** Signals termination. */
    private boolean isTerminated_;
    
    /** Communicator with the upper level. */
    private MonitoringCommunicator communicator_;
    
    /** Repository.*/
    LocalControllerRepository repository_;
    
    /**
     * Constructor.
     * 
     * @param localController           The local controller description
     * @param groupManagerAddress       The group manager address
     * @param dataQueue                 The data queue
     * @param infrastructureMonitoring  The infrastructure monitoring
     * @param callback                  The monitoring service callback
     * @param databaseSettings          The databaseSettings
     * @throws Exception                The exception
     */
    public VirtualMachineMonitorDataConsumer(LocalControllerDescription localController,
                                             LocalControllerRepository repository,
                                             MonitoringCommunicator communicator, 
                                             BlockingQueue<AggregatedVirtualMachineData> dataQueue,
                                             InfrastructureMonitoring infrastructureMonitoring,
                                             DatabaseSettings databaseSettings,
                                             VirtualMachineMonitoringListener callback) 
        throws Exception
    {
        
        log_.debug("Initializing virtual machine monitoring consumer");
        MonitoringThresholds monitoringThresholds = infrastructureMonitoring.getMonitoringSettings().getThresholds();
        localControllerId_ = localController.getId();
        dataQueue_ = dataQueue;
        callback_ = callback;
        repository_ = repository;
        crossingDetector_ = new ThresholdCrossingDetector(monitoringThresholds, localController.getTotalCapacity());
        communicator_  = communicator;
    }
   
    /**
     * Sends heartbeat data.
     * 
     * @param localControllerId         The local controller identifier
     * @throws InterruptedException 
     * @throws InterruptedException          The exception
     */
    private void sendHeartbeatData(String localControllerId) throws InterruptedException 
    {
        Guard.check(localControllerId);
        LocalControllerDataTransporter localControllerData = new LocalControllerDataTransporter(localControllerId, 
                                                                                                null);
        log_.debug("Sending local controller heartbeat information to group manager");        
        try
        {
            communicator_.sendHeartbeatData(localControllerData);
        }
        catch (IOException exception)
        {
            log_.debug(String.format("I/O error during data sending heartbeat (%s)! Did the group manager close " +
                    "its connection unexpectedly?", exception.getMessage()));
            throw new InterruptedException();
        }
    }
    
    /**
     * Sends regular data.
     * 
     * @param localControllerId     The local controller identifier
     * @param aggregatedData        The aggregated data
     * @throws InterruptedException 
     * @throws InterruptedException          The I/O exception
     */
    @SuppressWarnings("unchecked")
    private void sendRegularData(String localControllerId, ArrayList<AggregatedVirtualMachineData> aggregatedData)
                    throws InterruptedException 
    {
        Guard.check(localControllerId, aggregatedData);
        
        ArrayList<AggregatedVirtualMachineData> clonedData =
            (ArrayList<AggregatedVirtualMachineData>) aggregatedData.clone();
                
        LocalControllerDataTransporter localControllerData = 
            new LocalControllerDataTransporter(localControllerId, clonedData);
        
       // boolean isDetected = crossingDetector_.detectThresholdCrossing(localControllerData);
        boolean isDetected = false;
        
        log_.debug("Sending aggregated local controller summary information to group mananger");
        try
        {
            if (!isDetected)
            {
                repository_.addAggregatedVirtualMachineData(clonedData);
                communicator_.sendRegularData(localControllerData);
                log_.debug("No threshold crossing detected! Node seems stable for now!");
            }
            else
            {
                //send directly to GM to take into account the treshold crossing. 
                // hack ? we use the heartbeat message for that.
                communicator_.sendHeartbeatData(localControllerData);
            }
        }
        catch (IOException exception)
        {
            log_.debug(String.format("I/O error during data sending (%s)! Did the group manager close " +
                    "its connection unexpectedly?", exception.getMessage()));
            throw new InterruptedException();
        }
    }
    
    /** Run method. */
    public void run() 
    {
        ArrayList<AggregatedVirtualMachineData> aggregatedData = new ArrayList<AggregatedVirtualMachineData>();
        try
        {
            while (!isTerminated_)
            {                               
                log_.debug("Waiting for virtual machine monitoring data to arrive...");
                AggregatedVirtualMachineData virtualMachineData = dataQueue_.take();
                if (virtualMachineData == null)
                {
                    continue;
                }
                log_.debug(String.format("Received virtual machine %s data", 
                                         virtualMachineData.getVirtualMachineId()));
                
                
                if (virtualMachineData.getVirtualMachineId().equals("heartbeat"))
                {
                    
                    sendHeartbeatData(localControllerId_);
                    continue;
                }

                                
                aggregatedData.add(virtualMachineData);
                
                log_.debug(String.format("Current state of aggregated virtual machine data: %d / %d",
                                         aggregatedData.size(), 
                                         callback_.getNumberOfActiveVirtualMachines()));
                
                if (aggregatedData.size() == callback_.getNumberOfActiveVirtualMachines())
                {                    
                    sendRegularData(localControllerId_, aggregatedData);
                    aggregatedData.clear();
                }
            }   
        } 
        catch (InterruptedException exception)
        {
            log_.error("Virtual machine monitoring data consumer thread was interruped", exception);
        }  
        
        log_.debug("Virtual machine monitoring data consumer stopped!");
        terminate();
    }
    


    /**
     * Terminates the consumer.
     * @throws InterruptedException 
     */
    public void terminate() 
    {
        log_.debug("Terminating the virtual machine monitoring data consumer");
        isTerminated_ = true;
        communicator_.close();

    }

    

}
