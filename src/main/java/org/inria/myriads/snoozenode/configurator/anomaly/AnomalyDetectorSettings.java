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
package org.inria.myriads.snoozenode.configurator.anomaly;

import java.util.Map;

/**
 * Estimator settings.
 * 
 * @author Eugen Feller
 */
public final class AnomalyDetectorSettings 
{
   
    /** Enable the service.*/
    private boolean isEnabled_;
    
    /** Name of the detector (class).*/
    private String name_;
    
    /** Number of monitoring entries to take into account.*/
    private int numberOfMonitoringEntries_;
    
    
    /** Loop interval.*/
    private int interval_;
    
    /** Extra options.*/
    private Map<String, String> options_;

    
    /**
     * 
     */
    public AnomalyDetectorSettings()
    {
        super();
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name_;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        name_ = name;
    }

    /**
     * @return the options
     */
    public Map<String, String> getOptions()
    {
        return options_;
    }

    /**
     * @param options the options to set
     */
    public void setOptions(Map<String, String> options)
    {
        options_ = options;
    }

    /**
     * @return the numberOfMonitoringEntries
     */
    public int getNumberOfMonitoringEntries()
    {
        return numberOfMonitoringEntries_;
    }


    /**
     * 
     * Sets the number of monitoring entries.
     * 
     * @param numberOfMonitoringEntries number of monitoring entries
     */
    public void setNumberOfMonitoringEntries(int numberOfMonitoringEntries)
    {
        numberOfMonitoringEntries_ = numberOfMonitoringEntries;
    }

    /**
     * @return the interval
     */
    public int getInterval()
    {
        return interval_;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(int interval)
    {
        interval_ = interval;
    }

    /**
     * @return the isEnabled
     */
    public boolean isEnabled()
    {
        return isEnabled_;
    }

    /**
     * @param isEnabled the isEnabled to set
     */
    public void setEnabled(boolean isEnabled)
    {
        this.isEnabled_ = isEnabled;
    }

    
    
    
}
