import { Link } from 'react-router-dom';
import { Clock, Users, DollarSign,  CheckCircle, FileText, Activity } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { PageHeader, PageError } from '@/components/common';
import { useDashboard } from '@/hooks/useEmployee';
import { useAuth } from '@/contexts/AuthContext';
import { Role, TimesheetStatus } from '@/types';

const getTimesheetStatusBadge = (status?: TimesheetStatus) => {
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
      return <Badge variant="outline">No Timesheet</Badge>;
  }
};

const DashboardSkeleton = () => (
  <div className="space-y-6">
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      {[1, 2, 3, 4].map((i) => (
        <Card key={i}>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <Skeleton className="h-4 w-24" />
            <Skeleton className="h-4 w-4" />
          </CardHeader>
          <CardContent>
            <Skeleton className="h-8 w-16" />
            <Skeleton className="mt-1 h-3 w-32" />
          </CardContent>
        </Card>
      ))}
    </div>
    <Card>
      <CardHeader>
        <Skeleton className="h-5 w-32" />
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      </CardContent>
    </Card>
  </div>
);

export const DashboardPage = () => {
  const { user, hasRole } = useAuth();
  const { data: dashboard, isLoading, isError, error, refetch } = useDashboard();

  if (isLoading) return <DashboardSkeleton />;

  if (isError) {
    return (
      <PageError
        title="Failed to load dashboard"
        message={error?.response?.data?.message || 'Unable to load dashboard data.'}
        onRetry={() => refetch()}
      />
    );
  }

  const stats = dashboard?.stats;
  const personalInfo = dashboard?.personalInfo;
  const recentActivity = dashboard?.recentActivity || [];

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Welcome back, ${personalInfo?.firstName || user?.firstName}!`}
        description="Here's what's happening with your account today."
      />

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Current Timesheet</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {getTimesheetStatusBadge(stats?.currentTimesheetStatus)}
            <p className="mt-1 text-xs text-muted-foreground">
              <Link to="/timesheets" className="hover:underline">View timesheet →</Link>
            </p>
          </CardContent>
        </Card>

        {hasRole([Role.MANAGER, Role.ADMIN]) && (
          <>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Direct Reports</CardTitle>
                <Users className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{stats?.directReportsCount || 0}</div>
                <p className="text-xs text-muted-foreground">
                  <Link to="/manager/team" className="hover:underline">View team →</Link>
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Pending Timesheets</CardTitle>
                <FileText className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{stats?.pendingTimesheets || 0}</div>
                <p className="text-xs text-muted-foreground">
                  <Link to="/manager/timesheets" className="hover:underline">Review now →</Link>
                </p>
              </CardContent>
            </Card>
          </>
        )}

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">My Payroll</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">View</div>
            <p className="text-xs text-muted-foreground">
              <Link to="/payroll" className="hover:underline">Payment history →</Link>
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Quick Actions */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Quick Actions</CardTitle>
            <CardDescription>Common tasks and shortcuts</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-wrap gap-2">
            <Button asChild variant="outline" size="sm">
              <Link to="/timesheets">Submit Timesheet</Link>
            </Button>
            <Button asChild variant="outline" size="sm">
              <Link to="/profile">Update Profile</Link>
            </Button>
            <Button asChild variant="outline" size="sm">
              <Link to="/payroll">View Payslips</Link>
            </Button>
            {hasRole([Role.MANAGER, Role.ADMIN]) && (
              <Button asChild variant="outline" size="sm">
                <Link to="/manager/timesheets">Review Timesheets</Link>
              </Button>
            )}
          </CardContent>
        </Card>

        {/* Recent Activity */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <Activity className="h-4 w-4" /> Recent Activity
            </CardTitle>
          </CardHeader>
          <CardContent>
            {recentActivity.length > 0 ? (
              <div className="space-y-3">
                {recentActivity.slice(0, 5).map((activity, index) => (
                  <div key={index} className="flex items-start gap-3 text-sm">
                    <div className="rounded-full bg-muted p-1">
                      <CheckCircle className="h-3 w-3" />
                    </div>
                    <div>
                      <p className="font-medium">{activity.action}</p>
                      <p className="text-muted-foreground text-xs">{activity.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">No recent activity to display.</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default DashboardPage;

