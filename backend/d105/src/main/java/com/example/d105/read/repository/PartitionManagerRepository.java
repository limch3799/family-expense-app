package com.example.d105.read.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class PartitionManagerRepository {

    private final JdbcTemplate readJdbcTemplate;

    public PartitionManagerRepository(@Qualifier("readDataSource") DataSource readDataSource) {
        this.readJdbcTemplate = new JdbcTemplate(readDataSource);
    }

    public void createMonthlyPartitionIfNotExists(String tableName, String yearMonth) {
        String partitionName = tableName + "_" + yearMonth.replace("-", "_");
        String nextMonth = getNextMonth(yearMonth);

        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS d105_read.%s 
            PARTITION OF d105_read.%s 
            FOR VALUES FROM ('%s') TO ('%s')
            """, partitionName, tableName, yearMonth, nextMonth);

        readJdbcTemplate.execute(sql);
    }

    private String getNextMonth(String yearMonth) {
        String[] parts = yearMonth.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        if (month == 12) {
            return (year + 1) + "-01";
        } else {
            return year + "-" + String.format("%02d", month + 1);
        }
    }

    public void dropPartitionIfExists(String tableName, String yearMonth) {
        String partitionName = tableName + "_" + yearMonth.replace("-", "_");
        String sql = "DROP TABLE IF EXISTS d105_read." + partitionName;
        readJdbcTemplate.execute(sql);
    }
}