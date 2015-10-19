/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;

/**
 * Delta-disclosure privacy as proposed in:<br>
 * <br>
 * Justin Brickell and Vitaly Shmatikov:<br>
 * The Cost of Privacy: Destruction of Data-mining Utility in Anonymized Data Publishing<br>
 * Proceedings of the 14th ACM SIGKDD International Conference on Knowledge Discovery and Data Mining<br>
 * 2008
 *
 * @author Fabian Prasser
 */
public class DDisclosurePrivacy extends ExplicitPrivacyCriterion {

    /** SVUID */
    private static final long   serialVersionUID = 1543994581019659183L;

    /** Log 2. */
    private static final double LOG2             = Math.log(2);

    /** Parameter */
    private final double        d;

    /** The original distribution. */
    private double[]            distribution;

    /**
     * Computes log 2.
     *
     * @param num
     * @return
     */
    static final double log2(final double num) {
        return Math.log(num) / LOG2;
    }
    
    /**
     * Creates a new instance
     *
     * @param attribute
     * @param delta
     */
    public DDisclosurePrivacy(String attribute, double delta) {
        super(attribute, false, false);
        this.d = delta;
    }

    @Override
    public void initialize(DataManager manager) {
        super.initialize(manager);
        distribution = manager.getDistribution(attribute);
    }

    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {

        // For table t
        // Foreach class c
        //     Foreach sensitive value s
        //         abs(log(freq(s, c)/freq(s, t)) < delta
        
        // Init
        int[] buckets = entry.distributions[index].getBuckets();
        double count = entry.count;
        
        // For each value in c
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                double frequencyInT = distribution[buckets[i]];
                double frequencyInC = (double) buckets[i + 1] / count;
                double value = Math.abs(log2(frequencyInC / frequencyInT));
                if (value >= d) {
                    return false;
                }
            }
        }

        // check
        return true;
    }

	@Override
	public String toString() {
		return d+"-disclosure privacy for attribute '"+attribute+"'";
	}

    @Override
    public DDisclosurePrivacy clone() {
        return new DDisclosurePrivacy(this.getAttribute(), this.getD());
    }

    @Override
    public int getRequirements(){
        // Requires a distribution
        return ARXConfiguration.REQUIREMENT_DISTRIBUTION;
    }
    
    /**
     * Returns the parameter delta.
     *
     * @return
     */
    public double getD(){
        return d;
    }
}
