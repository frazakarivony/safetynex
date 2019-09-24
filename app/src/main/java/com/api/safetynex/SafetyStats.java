package com.api.safetynex;

import com.nexyad.jndksafetynex.CNxFullStat;
import com.nexyad.jndksafetynex.CNxUserStat;

public class SafetyStats {

    private CNxFullStat[] stats;
    private CNxUserStat inputStat;
    private CNxUserStat outputStat;

    public SafetyStats() {
    }

    public CNxFullStat[] getStats() {
        return stats;
    }

    public void setStats(CNxFullStat[] stats) {
        this.stats = stats;
    }

    public CNxUserStat getInputStat() {
        return inputStat;
    }

    public void setInputStat(CNxUserStat inputStat) {
        this.inputStat = inputStat;
    }

    public CNxUserStat getOutputStat() {
        return outputStat;
    }

    public void setOutputStat(CNxUserStat outputStat) {
        this.outputStat = outputStat;
    }
}
