package com.example.hrms.repositories;

import com.example.hrms.models.TimesheetEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, Long> {

    //find all entries for a timesheet
    List<TimesheetEntry> findByTimesheetIdOrderByWorkDateAsc(Long timesheetId);

    //find entry by timesheet and work date
    Optional<TimesheetEntry> findByTimesheetIdAndWorkDate(Long timesheetId, LocalDate workDate);

    //delete all entries for timesheet
    @Modifying
    @Query("DELETE FROM TimesheetEntry te WHERE te.timesheet.id = :timesheetId")
    void deleteByTimesheetId(@Param("timesheetId") Long timesheetId);

    //check if entry exists for timesheet and date
    boolean existsByTimesheetIdAndWorkDate(Long timesheetId, LocalDate workDate);

}
