package com.ekhaya.arsenalindex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    private Team homeTeam;
    private Team awayTeam;
    private String score;
}