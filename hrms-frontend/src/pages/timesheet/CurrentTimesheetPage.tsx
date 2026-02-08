import { useState, useEffect, useMemo } from 'react';
import { useCurrentTimesheet, useUpdateTimesheet, useSubmitTimesheet } from '@/hooks/useTimesheet';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { PageError } from '@/components/common';
import { Loader2, Save, Send, AlertCircle, Clock, Calendar, Edit } from 'lucide-react';
import { TimesheetStatus, type TimesheetEntryRequest, type TimesheetEntryResponse } from '@/types';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { cn } from '@/lib/utils';

// Day names for display
const DAYS_OF_WEEK = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

// Generate all dates for a week given the start date
const generateWeekDates = (weekStartDate: string): string[] => {
  const dates: string[] = [];
  const startDate = new Date(weekStartDate);
  for (let i = 0; i < 7; i++) {
    const date = new Date(startDate);
    date.setDate(startDate.getDate() + i);
    dates.push(date.toISOString().split('T')[0]);
  }
  return dates;
};

// Initialize entries for all 7 days
const initializeWeekEntries = (
  weekStartDate: string,
  existingEntries: TimesheetEntryResponse[]
): TimesheetEntryRequest[] => {
  const weekDates = generateWeekDates(weekStartDate);
  const entriesMap = new Map(existingEntries.map(e => [e.workDate, e]));

  return weekDates.map(date => {
    const existing = entriesMap.get(date);
    return {
      workDate: date,
      hoursWorked: existing?.hoursWorked || 0,
      description: existing?.description || '',
    };
  });
};

const getStatusBadge = (status: TimesheetStatus) => {
  switch (status) {
    case TimesheetStatus.DRAFT:
      return <Badge variant="secondary" className="gap-1"><Edit className="h-3 w-3" /> Draft</Badge>;
    case TimesheetStatus.SUBMITTED:
      return <Badge variant="warning" className="gap-1"><Clock className="h-3 w-3" /> Pending Review</Badge>;
    case TimesheetStatus.APPROVED:
      return <Badge variant="success">Approved</Badge>;
    case TimesheetStatus.DENIED:
      return <Badge variant="destructive">Denied - Action Required</Badge>;
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
};

const getStatusDescription = (status: TimesheetStatus) => {
  switch (status) {
    case TimesheetStatus.DRAFT:
      return 'You can edit and save your timesheet. Submit when ready for approval.';
    case TimesheetStatus.SUBMITTED:
      return 'Your timesheet has been submitted and is awaiting manager approval.';
    case TimesheetStatus.APPROVED:
      return 'Your timesheet has been approved. No further action required.';
    case TimesheetStatus.DENIED:
      return 'Your timesheet was denied. Please review the feedback, make corrections, and resubmit.';
    default:
      return '';
  }
};

const TimesheetSkeleton = () => (
  <Card>
    <CardHeader>
      <Skeleton className="h-6 w-48" />
      <Skeleton className="mt-2 h-4 w-64" />
    </CardHeader>
    <CardContent>
      <div className="space-y-3">
        {[1, 2, 3, 4, 5, 6, 7].map((i) => (
          <Skeleton key={i} className="h-20 w-full" />
        ))}
      </div>
    </CardContent>
  </Card>
);

// Validation helper
const validateHours = (hours: number): { valid: boolean; error?: string } => {
  if (hours < 0) return { valid: false, error: 'Hours cannot be negative' };
  if (hours > 24) return { valid: false, error: 'Hours cannot exceed 24' };
  return { valid: true };
};

export const CurrentTimesheetPage = () => {
  const { data: timesheet, isLoading, isError, error, refetch } = useCurrentTimesheet();
  const updateMutation = useUpdateTimesheet();
  const submitMutation = useSubmitTimesheet();

  const [entries, setEntries] = useState<TimesheetEntryRequest[]>([]);
  const [hasChanges, setHasChanges] = useState(false);
  const [validationErrors, setValidationErrors] = useState<Map<string, string>>(new Map());

  // Initialize entries when timesheet loads
  useEffect(() => {
    if (timesheet) {
      const weekEntries = initializeWeekEntries(timesheet.weekStartDate, timesheet.entries);
      setEntries(weekEntries);
      setHasChanges(false);
      setValidationErrors(new Map());
    }
  }, [timesheet]);

  // Calculate total hours
  const totalHours = useMemo(() => {
    return entries.reduce((sum, e) => sum + (e.hoursWorked || 0), 0);
  }, [entries]);

  // Check if all entries are valid
  const hasValidationErrors = validationErrors.size > 0;

  if (isLoading) return <TimesheetSkeleton />;

  if (isError) {
    return (
      <PageError
        title="Failed to load timesheet"
        message={error?.response?.data?.message || 'Unable to load current timesheet.'}
        onRetry={() => refetch()}
      />
    );
  }

  if (!timesheet) return null;

  const canEdit = timesheet.status === TimesheetStatus.DRAFT || timesheet.status === TimesheetStatus.DENIED;
  const canSubmit = timesheet.status === TimesheetStatus.DRAFT || timesheet.status === TimesheetStatus.DENIED;

  const handleHoursChange = (index: number, value: string) => {
    const hours = value === '' ? 0 : parseFloat(value);
    const validation = validateHours(hours);

    const newErrors = new Map(validationErrors);
    if (!validation.valid) {
      newErrors.set(entries[index].workDate, validation.error!);
    } else {
      newErrors.delete(entries[index].workDate);
    }
    setValidationErrors(newErrors);

    const newEntries = [...entries];
    newEntries[index] = { ...newEntries[index], hoursWorked: Math.max(0, Math.min(24, hours)) };
    setEntries(newEntries);
    setHasChanges(true);
  };

  const handleDescriptionChange = (index: number, value: string) => {
    const newEntries = [...entries];
    newEntries[index] = { ...newEntries[index], description: value };
    setEntries(newEntries);
    setHasChanges(true);
  };

  const handleSave = () => {
    if (hasValidationErrors) return;

    // Filter out entries with 0 hours and no description (optional: keep all for clarity)
    const entriesToSave = entries.filter(e => e.hoursWorked > 0 || e.description);

    updateMutation.mutate(
      { timesheetId: timesheet.id, data: { entries: entriesToSave.length > 0 ? entriesToSave : entries } },
      { onSuccess: () => setHasChanges(false) }
    );
  };

  const handleSubmit = () => {
    if (hasValidationErrors) return;

    const entriesToSave = entries.filter(e => e.hoursWorked > 0 || e.description);

    if (entriesToSave.length === 0) {
      return; // Cannot submit empty timesheet
    }

    if (hasChanges) {
      updateMutation.mutate(
        { timesheetId: timesheet.id, data: { entries: entriesToSave } },
        { onSuccess: () => submitMutation.mutate(timesheet.id) }
      );
    } else {
      submitMutation.mutate(timesheet.id);
    }
  };

  const formatDateDisplay = (dateStr: string, dayName: string) => {
    const date = new Date(dateStr + 'T00:00:00');
    return {
      dayName,
      dateStr: date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
      isWeekend: dayName === 'Saturday' || dayName === 'Sunday',
      isToday: dateStr === new Date().toISOString().split('T')[0],
    };
  };

  return (
    <div className="space-y-6">
      {/* Status Card */}
      <Card>
        <CardHeader className="pb-3">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div className="flex items-center gap-3">
              <div className="rounded-lg bg-primary/10 p-2">
                <Calendar className="h-5 w-5 text-primary" />
              </div>
              <div>
                <CardTitle className="text-lg">
                  Week of {new Date(timesheet.weekStartDate + 'T00:00:00').toLocaleDateString('en-US', {
                    month: 'long', day: 'numeric', year: 'numeric'
                  })}
                </CardTitle>
                <CardDescription>
                  {timesheet.weekStartDate} to {timesheet.weekEndDate}
                </CardDescription>
              </div>
            </div>
            <div className="flex items-center gap-2">
              {getStatusBadge(timesheet.status)}
            </div>
          </div>
        </CardHeader>
        <CardContent className="pt-0">
          <p className="text-sm text-muted-foreground">{getStatusDescription(timesheet.status)}</p>
        </CardContent>
      </Card>

      {/* Denial Reason Alert */}
      {timesheet.status === TimesheetStatus.DENIED && timesheet.denialReason && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Timesheet Denied by Manager</AlertTitle>
          <AlertDescription className="mt-2">
            <div className="rounded-md bg-destructive/10 p-3 mt-2">
              <p className="font-medium">Reason for denial:</p>
              <p className="mt-1">{timesheet.denialReason}</p>
            </div>
            <p className="mt-3 text-sm">
              Please review the feedback above, make the necessary corrections, and resubmit your timesheet.
            </p>
          </AlertDescription>
        </Alert>
      )}

      {/* Weekly Timesheet Entry Card */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" /> Time Entries
            </CardTitle>
            <div className="text-right">
              <div className="text-2xl font-bold text-primary">{totalHours.toFixed(1)}</div>
              <div className="text-xs text-muted-foreground">Total Hours</div>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {entries.map((entry, index) => {
              const { dayName, dateStr, isWeekend, isToday } = formatDateDisplay(entry.workDate, DAYS_OF_WEEK[index]);
              const hasError = validationErrors.has(entry.workDate);

              return (
                <div
                  key={entry.workDate}
                  className={cn(
                    "grid gap-4 rounded-lg border p-4 transition-colors",
                    isWeekend && "bg-muted/30",
                    isToday && "ring-2 ring-primary ring-offset-2",
                    hasError && "border-destructive"
                  )}
                >
                  <div className="grid gap-4 md:grid-cols-[140px_120px_1fr]">
                    {/* Day Label */}
                    <div className="flex flex-col justify-center">
                      <span className={cn(
                        "font-semibold",
                        isToday && "text-primary"
                      )}>
                        {dayName}
                        {isToday && <span className="ml-2 text-xs font-normal text-primary">(Today)</span>}
                      </span>
                      <span className="text-sm text-muted-foreground">{dateStr}</span>
                    </div>

                    {/* Hours Input */}
                    <div className="space-y-1">
                      <Input
                        type="number"
                        min="0"
                        max="24"
                        step="0.5"
                        value={entry.hoursWorked || ''}
                        onChange={(e) => handleHoursChange(index, e.target.value)}
                        disabled={!canEdit || updateMutation.isPending}
                        className={cn(
                          "w-full text-center font-medium",
                          hasError && "border-destructive focus-visible:ring-destructive"
                        )}
                        placeholder="0"
                      />
                      {hasError && (
                        <p className="text-xs text-destructive">{validationErrors.get(entry.workDate)}</p>
                      )}
                      <p className="text-xs text-muted-foreground text-center">hours</p>
                    </div>

                    {/* Description Input */}
                    <div>
                      <Textarea
                        value={entry.description || ''}
                        onChange={(e) => handleDescriptionChange(index, e.target.value)}
                        disabled={!canEdit || updateMutation.isPending}
                        placeholder="What did you work on? (optional)"
                        className="min-h-[60px] resize-none"
                        maxLength={500}
                      />
                      <p className="text-xs text-muted-foreground mt-1 text-right">
                        {entry.description?.length || 0}/500
                      </p>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Summary */}
          <div className="mt-6 flex items-center justify-between rounded-lg bg-muted/50 p-4">
            <div className="flex items-center gap-4">
              <div>
                <span className="text-sm text-muted-foreground">Weekday Hours</span>
                <p className="font-semibold">
                  {entries.slice(0, 5).reduce((sum, e) => sum + (e.hoursWorked || 0), 0).toFixed(1)}
                </p>
              </div>
              <div className="h-8 w-px bg-border" />
              <div>
                <span className="text-sm text-muted-foreground">Weekend Hours</span>
                <p className="font-semibold">
                  {entries.slice(5, 7).reduce((sum, e) => sum + (e.hoursWorked || 0), 0).toFixed(1)}
                </p>
              </div>
            </div>
            <div className="text-right">
              <span className="text-sm text-muted-foreground">Week Total</span>
              <p className="text-2xl font-bold text-primary">{totalHours.toFixed(1)} hrs</p>
            </div>
          </div>
        </CardContent>

        {/* Actions Footer */}
        {canEdit && (
          <CardFooter className="flex flex-col gap-3 border-t bg-muted/20 sm:flex-row sm:justify-between">
            <div className="text-sm text-muted-foreground">
              {hasChanges ? (
                <span className="text-amber-600">• Unsaved changes</span>
              ) : (
                <span>All changes saved</span>
              )}
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                onClick={handleSave}
                disabled={!hasChanges || updateMutation.isPending || hasValidationErrors}
              >
                {updateMutation.isPending ? (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                ) : (
                  <Save className="mr-2 h-4 w-4" />
                )}
                Save Draft
              </Button>
              {canSubmit && (
                <Button
                  onClick={handleSubmit}
                  disabled={submitMutation.isPending || updateMutation.isPending || hasValidationErrors || totalHours === 0}
                >
                  {submitMutation.isPending ? (
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  ) : (
                    <Send className="mr-2 h-4 w-4" />
                  )}
                  {timesheet.status === TimesheetStatus.DENIED ? 'Resubmit for Approval' : 'Submit for Approval'}
                </Button>
              )}
            </div>
          </CardFooter>
        )}

        {/* Read-only footer for submitted/approved */}
        {!canEdit && (
          <CardFooter className="border-t bg-muted/20">
            <div className="flex w-full items-center justify-between">
              <p className="text-sm text-muted-foreground">
                {timesheet.status === TimesheetStatus.SUBMITTED && 'Submitted on ' +
                  new Date(timesheet.submittedAt!).toLocaleDateString('en-US', {
                    month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit'
                  })}
                {timesheet.status === TimesheetStatus.APPROVED && 'Approved on ' +
                  new Date(timesheet.reviewedAt!).toLocaleDateString('en-US', {
                    month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit'
                  })}
              </p>
              {timesheet.reviewedBy && timesheet.status === TimesheetStatus.APPROVED && (
                <p className="text-sm text-muted-foreground">
                  by {timesheet.reviewedBy.name}
                </p>
              )}
            </div>
          </CardFooter>
        )}
      </Card>
    </div>
  );
};

export default CurrentTimesheetPage;

