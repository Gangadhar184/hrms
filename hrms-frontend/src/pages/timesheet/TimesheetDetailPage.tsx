import { useParams, Link } from 'react-router-dom';
import { useTimesheetById } from '@/hooks/useTimesheet';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { PageError } from '@/components/common';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { ArrowLeft, AlertCircle, CheckCircle2, Clock, XCircle } from 'lucide-react';
import { TimesheetStatus } from '@/types';

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

const DetailSkeleton = () => (
  <div className="space-y-6">
    <Skeleton className="h-8 w-32" />
    <Card>
      <CardHeader>
        <Skeleton className="h-6 w-48" />
        <Skeleton className="mt-2 h-4 w-64" />
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {[1, 2, 3, 4, 5].map((i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      </CardContent>
    </Card>
  </div>
);

export const TimesheetDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const timesheetId = id ? parseInt(id) : undefined;
  const { data: timesheet, isLoading, isError, error, refetch } = useTimesheetById(timesheetId);

  if (isLoading) return <DetailSkeleton />;

  if (isError) {
    return (
      <PageError
        title="Failed to load timesheet"
        message={error?.response?.data?.message || 'Unable to load timesheet details.'}
        onRetry={() => refetch()}
      />
    );
  }

  if (!timesheet) return null;

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
    });
  };

  const formatDateTime = (dateStr?: string) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" asChild>
          <Link to="/timesheets/history">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to History
          </Link>
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Timesheet Details</CardTitle>
              <CardDescription>
                Week of {formatDate(timesheet.weekStartDate)} - {formatDate(timesheet.weekEndDate)}
              </CardDescription>
            </div>
            {getStatusBadge(timesheet.status)}
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          {timesheet.denialReason && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>
                <strong>Denial Reason:</strong> {timesheet.denialReason}
              </AlertDescription>
            </Alert>
          )}

          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Date</TableHead>
                <TableHead className="text-right">Hours</TableHead>
                <TableHead>Description</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {timesheet.entries.map((entry) => (
                <TableRow key={entry.id}>
                  <TableCell className="font-medium">{formatDate(entry.workDate)}</TableCell>
                  <TableCell className="text-right">{entry.hoursWorked.toFixed(1)}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {entry.description || '-'}
                  </TableCell>
                </TableRow>
              ))}
              <TableRow>
                <TableCell className="font-bold">Total</TableCell>
                <TableCell className="text-right font-bold text-primary">
                  {timesheet.totalHours.toFixed(1)}
                </TableCell>
                <TableCell />
              </TableRow>
            </TableBody>
          </Table>

          <div className="grid gap-4 rounded-lg border p-4 md:grid-cols-3">
            <div className="flex items-center gap-3">
              <Clock className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Submitted</p>
                <p className="font-medium">{formatDateTime(timesheet.submittedAt)}</p>
              </div>
            </div>
            {timesheet.reviewedAt && (
              <div className="flex items-center gap-3">
                {timesheet.status === TimesheetStatus.APPROVED ? (
                  <CheckCircle2 className="h-5 w-5 text-green-500" />
                ) : (
                  <XCircle className="h-5 w-5 text-destructive" />
                )}
                <div>
                  <p className="text-sm text-muted-foreground">Reviewed</p>
                  <p className="font-medium">{formatDateTime(timesheet.reviewedAt)}</p>
                </div>
              </div>
            )}
            {timesheet.reviewedBy && (
              <div>
                <p className="text-sm text-muted-foreground">Reviewed By</p>
                <p className="font-medium">{timesheet.reviewedBy.name}</p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default TimesheetDetailPage;

