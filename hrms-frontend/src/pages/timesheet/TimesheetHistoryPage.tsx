import { Link } from 'react-router-dom';
import { useTimesheetHistory } from '@/hooks/useTimesheet';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { PageError, EmptyState } from '@/components/common';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Clock, Eye } from 'lucide-react';
import { TimesheetStatus } from '@/types';

const getStatusBadge = (status: TimesheetStatus) => {
  switch (status) {
    case TimesheetStatus.DRAFT:
      return <Badge variant="secondary">Draft</Badge>;
    case TimesheetStatus.SUBMITTED:
      return <Badge variant="warning">Pending</Badge>;
    case TimesheetStatus.APPROVED:
      return <Badge variant="success">Approved</Badge>;
    case TimesheetStatus.DENIED:
      return <Badge variant="destructive">Denied</Badge>;
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
};

const HistorySkeleton = () => (
  <Card>
    <CardContent className="p-0">
      <div className="space-y-4 p-4">
        {[1, 2, 3, 4, 5].map((i) => (
          <Skeleton key={i} className="h-12 w-full" />
        ))}
      </div>
    </CardContent>
  </Card>
);

export const TimesheetHistoryPage = () => {
  const { data: timesheets, isLoading, isError, error, refetch } = useTimesheetHistory();

  if (isLoading) return <HistorySkeleton />;

  if (isError) {
    return (
      <PageError
        title="Failed to load history"
        message={error?.response?.data?.message || 'Unable to load timesheet history.'}
        onRetry={() => refetch()}
      />
    );
  }

  if (!timesheets || timesheets.length === 0) {
    return (
      <EmptyState
        icon={Clock}
        title="No timesheet history"
        description="You don't have any past timesheets yet."
      />
    );
  }

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  return (
    <Card>
      <CardContent className="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Week</TableHead>
              <TableHead>Period</TableHead>
              <TableHead className="text-right">Total Hours</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Submitted</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {timesheets.map((timesheet) => (
              <TableRow key={timesheet.id}>
                <TableCell className="font-medium">
                  {formatDate(timesheet.weekStartDate)}
                </TableCell>
                <TableCell className="text-muted-foreground">
                  {formatDate(timesheet.weekStartDate)} - {formatDate(timesheet.weekEndDate)}
                </TableCell>
                <TableCell className="text-right font-semibold">
                  {timesheet.totalHours.toFixed(1)}
                </TableCell>
                <TableCell>{getStatusBadge(timesheet.status)}</TableCell>
                <TableCell className="text-muted-foreground">
                  {timesheet.submittedAt
                    ? formatDate(timesheet.submittedAt)
                    : '-'}
                </TableCell>
                <TableCell className="text-right">
                  <Button variant="ghost" size="sm" asChild>
                    <Link to={`/timesheets/${timesheet.id}`}>
                      <Eye className="mr-2 h-4 w-4" />
                      View
                    </Link>
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
};

export default TimesheetHistoryPage;

