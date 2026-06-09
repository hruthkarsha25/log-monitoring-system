package com.project.logmonitoringsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogStatsDTO {
   private long totalLogs;
   private long infoLogs;
   private long warnLogs;
   private long errorLogs;
   private long logsToday;
   private long logsLast24hours;

   Map<String, Long> logsByService;

   Map<String, Long> logsByLevel;
}
