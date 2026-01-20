package com.example.taskandbookingmanagement.dto;

import jakarta.validation.constraints.NotNull;

public class TaskDecisionRequest {

    public enum Decision { APPROVE, REJECT }

    @NotNull
    private Decision decision;

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }
}
