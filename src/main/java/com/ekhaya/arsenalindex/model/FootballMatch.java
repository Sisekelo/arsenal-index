package com.ekhaya.arsenalindex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FootballMatch {
    private Team homeTeam;
    private Team awayTeam;
    private String winner;
    private String result;

    public FootballMatch(Team homeTeam, Team awayTeam, String winner) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.winner = winner;
        calculateResult();
    }

    private void calculateResult() {
        boolean isArsenalHome = homeTeam.getId() == 57;
        this.result = switch (winner) {
            case "HOME_TEAM" -> isArsenalHome ? "win" : "loss";
            case "AWAY_TEAM" -> isArsenalHome ? "loss" : "win";
            default -> "draw";
        };
    }

    public String getResult() {
        return result;
    }

    public String getOpponent() {
        return homeTeam.getId() == 57 ? awayTeam.getName() : homeTeam.getName();
    }
} 