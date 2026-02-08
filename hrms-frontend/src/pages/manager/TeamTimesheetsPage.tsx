import { useState } from 'react';
import { useTeamTimesheets, useApproveTimesheet, useDenyTimesheet, useTimesheetByIdForManager } from '@/hooks/useManager';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Select, SelectOption } from '@/components/ui/select';
import { PageHeader, PageError, EmptyState } from '@/components/common';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { ClipboardList, CheckCircle, XCircle, Loader2, Eye, Calendar, Clock, User } from 'lucide-react';
import { TimesheetStatus } from '@/types';
import { cn } from '@/lib/utils';

// Day names for display
const DAYS_OF_WEEK = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

const getStatusBadge = (status: TimesheetStatus) => {
  switch (status) {
    case TimesheetStatus.DRAFT:
      return <Badge variant="secondary">Draft</Badge>;
    case TimesheetStatus.SUBMITTED:
      return <Badge variant="warning">Pending Review</Badge>;
    case TimesheetStatus.APPROVED:
      return <Badge variant="success">Approved</Badge>;
    case TimesheetStatus.DENIED:
      return <Badge variant="destructive">Denied</Badge>;
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
};

const formatDate = (dateStr: string) => {
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
};

const formatDateTime = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit'
  });
};

// Timesheet Detail Dialog Component
const TimesheetDetailDialog = ({
  open,
  onOpenChange,
  timesheetId,
  onApprove,
  onDeny,
  isApproving,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  timesheetId: number | null;
  onApprove: (id: number) => void;
  onDeny: (id: number) => void;
  isApproving: boolean;
}) => {
  const { data: timesheet, isLoading } = useTimesheetByIdForManager(timesheetId || undefined);

  if (!open) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <ClipboardList className="h-5 w-5" />
            Timesheet Details
          </DialogTitle>
          {timesheet && (
            <DialogDescription>
              Review {timesheet.employee.name}'s timesheet before taking action
            </DialogDescription>
          )}
        </DialogHeader>

        {isLoading ? (
          <div className="space-y-4 py-4">
            <Skeleton className="h-8 w-48" />
            <Skeleton className="h-32 w-full" />
            <Skeleton className="h-32 w-full" />
          </div>
        ) : timesheet ? (
          <div className="space-y-6 py-4">
            {/* Employee & Week Info */}
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex items-center gap-3">
                <div className="rounded-full bg-primary/10 p-2">
                  <User className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <p className="font-semibold">{timesheet.employee.name}</p>
                  <p className="text-sm text-muted-foreground">Employee</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="rounded-full bg-muted p-2">
                  <Calendar className="h-5 w-5 text-muted-foreground" />
                </div>
                <div>
                  <p className="font-semibold">
                    {formatDate(timesheet.weekStartDate)} - {formatDate(timesheet.weekEndDate)}
                  </p>
                  <p className="text-sm text-muted-foreground">Week Period</p>
                </div>
              </div>
            </div>

            <Separator />

            {/* Daily Entries */}
            <div>
              <h4 className="font-semibold mb-3 flex items-center gap-2">
                <Clock className="h-4 w-4" /> Daily Time Entries
              </h4>
              <div className="rounded-lg border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Day</TableHead>
                      <TableHead>Date</TableHead>
                      <TableHead className="text-right">Hours</TableHead>
                      <TableHead>Description</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {timesheet.entries.length > 0 ? (
                      timesheet.entries.map((entry) => {
                        const dayIndex = new Date(entry.workDate + 'T00:00:00').getDay();
                        const dayName = DAYS_OF_WEEK[(dayIndex + 6) % 7]; // Adjust for Monday start
                        const isWeekend = dayName === 'Saturday' || dayName === 'Sunday';

                        return (
                          <TableRow key={entry.id} className={cn(isWeekend && "bg-muted/30")}>
                            <TableCell className="font-medium">{dayName}</TableCell>
                            <TableCell className="text-muted-foreground">
                              {formatDate(entry.workDate)}
                            </TableCell>
                            <TableCell className="text-right font-semibold">
                              {entry.hoursWorked.toFixed(1)}
                            </TableCell>
                            <TableCell className="max-w-[200px] truncate">
                              {entry.description || <span className="text-muted-foreground italic">No description</span>}
                            </TableCell>
                          </TableRow>
                        );
                      })
                    ) : (
                      <TableRow>
                        <TableCell colSpan={4} className="text-center text-muted-foreground py-8">
                          No time entries recorded
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </div>
            </div>

            {/* Summary */}
            <div className="flex items-center justify-between rounded-lg bg-muted/50 p-4">
              <div>
                <span className="text-sm text-muted-foreground">Submitted</span>
                <p className="font-medium">
                  {timesheet.submittedAt ? formatDateTime(timesheet.submittedAt) : 'Not submitted'}
                </p>
              </div>
              <div className="text-right">
                <span className="text-sm text-muted-foreground">Total Hours</span>
                <p className="text-2xl font-bold text-primary">{timesheet.totalHours.toFixed(1)}</p>
              </div>
            </div>
          </div>
        ) : (
          <div className="py-8 text-center text-muted-foreground">
            Unable to load timesheet details
          </div>
        )}

        {/* Actions */}
        {timesheet && timesheet.status === TimesheetStatus.SUBMITTED && (
          <DialogFooter className="flex-col gap-2 sm:flex-row">
            <Button variant="outline" onClick={() => onOpenChange(false)}>
              Close
            </Button>
            <Button
              variant="outline"
              className="text-destructive hover:text-destructive"
              onClick={() => {
                onOpenChange(false);
                onDeny(timesheet.id);
              }}
            >
              <XCircle className="mr-2 h-4 w-4" />
              Deny Timesheet
            </Button>
            <Button
              onClick={() => {
                onApprove(timesheet.id);
                onOpenChange(false);
              }}
              disabled={isApproving}
            >
              {isApproving ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <CheckCircle className="mr-2 h-4 w-4" />
              )}
              Approve Timesheet
            </Button>
          </DialogFooter>
        )}

        {timesheet && timesheet.status !== TimesheetStatus.SUBMITTED && (
          <DialogFooter>
            <Button variant="outline" onClick={() => onOpenChange(false)}>
              Close
            </Button>
          </DialogFooter>
        )}
      </DialogContent>
    </Dialog>
  );
};

export const TeamTimesheetsPage = () => {
  const [statusFilter, setStatusFilter] = useState<TimesheetStatus | ''>('');
  const [page, setPage] = useState(0);

  // Dialog states
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [denyDialogOpen, setDenyDialogOpen] = useState(false);
  const [selectedTimesheetId, setSelectedTimesheetId] = useState<number | null>(null);
  const [denyReason, setDenyReason] = useState('');

  const { data: timesheets, isLoading, isError, error, refetch } = useTeamTimesheets({
    status: statusFilter || undefined,
    page,
    size: 10,
  });

  const approveMutation = useApproveTimesheet();
  const denyMutation = useDenyTimesheet();

  const handleApprove = (timesheetId: number) => {
    approveMutation.mutate(timesheetId);
  };

  const handleDenyClick = (timesheetId: number) => {
    setSelectedTimesheetId(timesheetId);
    setDenyReason('');
    setDenyDialogOpen(true);
  };

  const handleDenySubmit = () => {
    if (selectedTimesheetId && denyReason.trim()) {
      denyMutation.mutate(
        { timesheetId: selectedTimesheetId, reason: denyReason },
        { onSuccess: () => { setDenyDialogOpen(false); setSelectedTimesheetId(null); setDenyReason(''); } }
      );
    }
  };

  const handleViewDetails = (timesheetId: number) => {
    setSelectedTimesheetId(timesheetId);
    setDetailDialogOpen(true);
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <PageHeader title="Team Timesheets" description="Review and manage team timesheets" />
        <Card><CardContent className="p-4"><div className="space-y-4">{[1, 2, 3, 4, 5].map((i) => (<Skeleton key={i} className="h-12 w-full" />))}</div></CardContent></Card>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="space-y-6">
        <PageHeader title="Team Timesheets" description="Review and manage team timesheets" />
        <PageError title="Failed to load timesheets" message={error?.response?.data?.message || 'Unable to load team timesheets.'} onRetry={() => refetch()} />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader title="Team Timesheets" description="Review and manage team timesheets">
        <Select value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value as TimesheetStatus | ''); setPage(0); }}>
          <SelectOption value="">All Statuses</SelectOption>
          <SelectOption value={TimesheetStatus.SUBMITTED}>Pending</SelectOption>
          <SelectOption value={TimesheetStatus.APPROVED}>Approved</SelectOption>
          <SelectOption value={TimesheetStatus.DENIED}>Denied</SelectOption>
        </Select>
      </PageHeader>

      {!timesheets?.content || timesheets.content.length === 0 ? (
        <EmptyState icon={ClipboardList} title="No timesheets found" description={statusFilter ? `No ${statusFilter.toLowerCase()} timesheets found.` : 'No team timesheets to display.'} />
      ) : (
        <>
          <Card>
            <CardContent className="p-0">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Employee</TableHead>
                    <TableHead>Week</TableHead>
                    <TableHead className="text-right">Hours</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Submitted</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {timesheets.content.map((ts) => (
                    <TableRow key={ts.id} className="cursor-pointer hover:bg-muted/50" onClick={() => handleViewDetails(ts.id)}>
                      <TableCell className="font-medium">{ts.employeeName}</TableCell>
                      <TableCell className="text-muted-foreground">{formatDate(ts.weekStartDate)} - {formatDate(ts.weekEndDate)}</TableCell>
                      <TableCell className="text-right font-semibold">{ts.totalHours.toFixed(1)}</TableCell>
                      <TableCell>{getStatusBadge(ts.status)}</TableCell>
                      <TableCell className="text-muted-foreground">{ts.submittedAt ? formatDate(ts.submittedAt) : '-'}</TableCell>
                      <TableCell className="text-right" onClick={(e) => e.stopPropagation()}>
                        <div className="flex justify-end gap-2">
                          <Button variant="ghost" size="sm" onClick={() => handleViewDetails(ts.id)}>
                            <Eye className="h-4 w-4" />
                          </Button>
                          {ts.status === TimesheetStatus.SUBMITTED && (
                            <>
                              <Button variant="outline" size="sm" onClick={() => handleApprove(ts.id)} disabled={approveMutation.isPending}>
                                {approveMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <><CheckCircle className="mr-1 h-4 w-4" /> Approve</>}
                              </Button>
                              <Button variant="outline" size="sm" onClick={() => handleDenyClick(ts.id)} className="text-destructive hover:text-destructive">
                                <XCircle className="mr-1 h-4 w-4" /> Deny
                              </Button>
                            </>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>

          {timesheets.totalPages > 1 && (
            <div className="flex justify-center gap-2">
              <Button variant="outline" size="sm" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={timesheets.first}>Previous</Button>
              <span className="flex items-center px-4 text-sm text-muted-foreground">Page {timesheets.currentPage + 1} of {timesheets.totalPages}</span>
              <Button variant="outline" size="sm" onClick={() => setPage((p) => p + 1)} disabled={timesheets.last}>Next</Button>
            </div>
          )}
        </>
      )}

      {/* Timesheet Detail Dialog */}
      <TimesheetDetailDialog
        open={detailDialogOpen}
        onOpenChange={setDetailDialogOpen}
        timesheetId={selectedTimesheetId}
        onApprove={handleApprove}
        onDeny={handleDenyClick}
        isApproving={approveMutation.isPending}
      />

      {/* Deny Dialog */}
      <Dialog open={denyDialogOpen} onOpenChange={setDenyDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Deny Timesheet</DialogTitle>
            <DialogDescription>
              Please provide a reason for denying this timesheet. The employee will see this feedback.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="denyReason">Reason for Denial <span className="text-destructive">*</span></Label>
              <Textarea
                id="denyReason"
                value={denyReason}
                onChange={(e) => setDenyReason(e.target.value)}
                placeholder="Enter a clear reason for denial so the employee can correct their timesheet..."
                className="min-h-[100px]"
              />
              <p className="text-xs text-muted-foreground">
                Be specific about what needs to be corrected (e.g., "Missing project codes for Wednesday entries")
              </p>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDenyDialogOpen(false)}>Cancel</Button>
            <Button variant="destructive" onClick={handleDenySubmit} disabled={!denyReason.trim() || denyMutation.isPending}>
              {denyMutation.isPending ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <XCircle className="mr-2 h-4 w-4" />}
              Deny Timesheet
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default TeamTimesheetsPage;

