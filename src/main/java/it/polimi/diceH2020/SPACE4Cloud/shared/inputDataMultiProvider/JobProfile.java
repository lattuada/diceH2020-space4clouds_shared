/*
 * Copyright 2015 deib-polimi
 * Contact: deib-polimi <michele.ciavotta@polimi.it>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Data
public class JobProfile {

	@JsonUnwrapped private Map<String, Double> profileMap;

	public JobProfile () {
		profileMap = new TreeMap<> ();
	}

	public JobProfile (Map<String, Double> profileMap) {
		this.profileMap = profileMap;
	}

	@JsonIgnore
	public boolean validate () {
		boolean valid = profileMap != null && ! profileMap.isEmpty ()
				&& profileMap.containsKey ("h") && profileMap.containsKey ("x");

		if (valid) {
			Iterator<Double> iterator = profileMap.values ().iterator ();
			while (valid && iterator.hasNext ()) {
				double value = iterator.next ();
				valid = value >= 0.;
			}
		}

		return valid;
	}

	@JsonIgnore
	public void put (String key, double value) {
		profileMap.put(key, value);
	}

	@JsonIgnore
	public double get (String key) throws IllegalArgumentException {
		if (! profileMap.containsKey (key)) {
			throw new IllegalArgumentException (
					String.format ("'%s' is not contained in this JobProfile", key));
		}
		return profileMap.get(key);
	}
}
