/**
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
package it.polimi.diceH2020.SPACE4Cloud.shared.solution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.PrivateCloudParameters;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.TypeVMJobClassKey;
import it.polimi.diceH2020.SPACE4Cloud.shared.settings.Scenario;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Data
public class Solution {

	private List<SolutionPerJob> lstSolutions = new ArrayList<>();
	private List<Phase> lstPhases = new ArrayList<>();
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Optional<PrivateCloudParameters> privateCloudParameters; 
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Optional<Scenario> scenario; 
	
	private String id;
	private String provider;

	private Boolean feasible;

	@JsonUnwrapped
	@JsonInclude(Include.NON_NULL)
	private Map<Integer,Boolean> activeNodes;
	@JsonUnwrapped
	@JsonInclude(Include.NON_NULL)
	private Map<String,Integer> numVMPerNodePerClass;

	public Solution() {
		privateCloudParameters = Optional.empty(); 
	}

	public Solution(String id) {
		this.id = id;
		privateCloudParameters = Optional.empty(); 
	}

	private Double cost = -1.0;
	private Double penalty = 0.0;

	@JsonIgnore
	private IEvaluator evaluator;

	private Boolean evaluated = false;

	public Boolean getFeasible() {
		if (evaluated) {
			if(feasible != null) {
				return feasible;
			} else {
				return lstSolutions.stream().allMatch(SolutionPerJob::getFeasible);
			}
		} else return Boolean.FALSE;
	}

	public SolutionPerJob getSolutionPerJob(int pos) {
		return lstSolutions.get(pos);
	}

	public void addSolutionPerJob(SolutionPerJob solPerJob) {
		lstSolutions.add(solPerJob);
		solPerJob.setParentID(this.id);
	}
	
	@JsonIgnore
	public List<TypeVMJobClassKey> getPairsTypeVMJobClass() {
		return lstSolutions.stream().map(sol -> new TypeVMJobClassKey(sol.getId(), sol.getTypeVMselected().getId())).collect(toList());
	}

	@JsonIgnore
	public List<Double> getLstNumberCores() {
		return getByFunctional(SolutionPerJob::getNumCores);
	}

	@JsonIgnore
	public List<Double> getListDeltabar() {
		return getByFunctional(SolutionPerJob::getDeltaBar);
	}

	@JsonIgnore
	public List<Double> getListRhobar() {
		return lstSolutions.stream().map(SolutionPerJob::getRhoBar).collect(toList());
	}

	@JsonIgnore
	public List<Double> getListSigmaBar() {
		return getByFunctional(SolutionPerJob::getSigmaBar);
	}

	@JsonIgnore
	private <R> List<R> getByFunctional(Function<SolutionPerJob, R> mapper) {
		return lstSolutions.stream().map(mapper).collect(toList());
	}

	@JsonIgnore
	public void addPhase(Phase ph) {
		this.lstPhases.add(ph);
	}

	@JsonIgnore
	public Long getOptimizationTime() {
		return lstPhases.stream().mapToLong(Phase::getDuration).sum();
	}

	public String toStringReduced() {
		StringJoiner sj = new StringJoiner("\t", "", "");
		sj.add("solID=" + id).add("solFeas=" + this.getFeasible().toString()).add("cost=" + BigDecimal.valueOf(this.getCost()).toString());
		sj.add("totalDuration=" + this.getOptimizationTime().toString());
		lstPhases.forEach(ph ->
				sj.add("phase=" + ph.getId().toString()).add("duration=" + ph.getDuration())
		);
		lstSolutions.forEach(s ->
				sj.add("jobClass=" + s.getId()).add("typeVM=" + s.getTypeVMselected().getId())
						.add("numVM=" + s.getNumberVM()).add("numReserved=" + s.getNumReservedVM())
						.add("numOnDemand=" + s.getNumOnDemandVM())
						.add("numSpot=" + s.getNumSpotVM()).add("jobFeas=" + s.getFeasible().toString())
		);
		return sj.toString();
	}

	public boolean validate() {
		return (this.id != null && ! this.id.equals("") &&
				lstSolutions.stream().map(SolutionPerJob::validate).allMatch(r -> r));
	}

}
