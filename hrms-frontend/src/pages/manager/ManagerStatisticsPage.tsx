import { useManagerStatistics, usePendingTimesheetsCount } from '@/hooks/useManager';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { PageHeader, PageError } from '@/components/common';
import { Users, ClipboardList, Clock, BarChart3 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';

const StatisticsSkeleton = () => (
  <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
    {[1, 2, 3].map((i) => (
      <Card key={i}>
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <Skeleton className="h-4 w-24" />
          <Skeleton className="h-4 w-4" />
        </CardHeader>
        <CardContent>
          <Skeleton className="h-10 w-16" />
          <Skeleton className="mt-2 h-3 w-32" />
        </CardContent>
      </Card>
    ))}
  </div>
);

export const ManagerStatisticsPage = () => {
  const {
    data: statistics,
    isLoading: statsLoading,
    isError: statsError,
    error: statsErrorData,
    refetch: refetchStats,
  } = useManagerStatistics();

  const {
    data: pendingCount,
    isLoading: pendingLoading,
  } = usePendingTimesheetsCount();

  const isLoading = statsLoading || pendingLoading;

  if (isLoading) {
    return (
      <div className="space-y-6">
        <PageHeader
          title="Manager Statistics"
          description="Overview of your team's performance and activity"
        />
        <StatisticsSkeleton />
      </div>
    );
  }

  if (statsError) {
    return (
      <div className="space-y-6">
        <PageHeader
          title="Manager Statistics"
          description="Overview of your team's performance and activity"
        />
        <PageError
          title="Failed to load statistics"
          message={statsErrorData?.response?.data?.message || 'Unable to load manager statistics.'}
          onRetry={() => refetchStats()}
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Manager Statistics"
        description="Overview of your team's performance and activity"
      />

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Direct Reports</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{statistics?.directReportsCount || 0}</div>
            <p className="text-sm text-muted-foreground mt-1">
              Team members reporting to you
            </p>
            <Button variant="link" className="mt-2 h-auto p-0" asChild>
              <Link to="/manager/team">View team →</Link>
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Pending Timesheets</CardTitle>
            <ClipboardList className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-amber-500">
              {pendingCount ?? statistics?.pendingTimesheetsCount ?? 0}
            </div>
            <p className="text-sm text-muted-foreground mt-1">
              Awaiting your review
            </p>
            {(pendingCount ?? statistics?.pendingTimesheetsCount ?? 0) > 0 && (
              <Button variant="link" className="mt-2 h-auto p-0" asChild>
                <Link to="/manager/timesheets">Review now →</Link>
              </Button>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Quick Actions</CardTitle>
            <BarChart3 className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent className="space-y-2">
            <Button variant="outline" size="sm" className="w-full justify-start" asChild>
              <Link to="/manager/timesheets">
                <Clock className="mr-2 h-4 w-4" />
                Review Timesheets
              </Link>
            </Button>
            <Button variant="outline" size="sm" className="w-full justify-start" asChild>
              <Link to="/manager/team">
                <Users className="mr-2 h-4 w-4" />
                View Team
              </Link>
            </Button>
          </CardContent>
        </Card>
      </div>

      {/* Additional Statistics Section */}
      <Card>
        <CardHeader>
          <CardTitle>Team Overview</CardTitle>
          <CardDescription>Summary of your team's current status</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2">
            <div className="rounded-lg border p-4">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Users className="h-4 w-4" />
                Total Team Size
              </div>
              <p className="mt-2 text-2xl font-semibold">
                {statistics?.directReportsCount || 0} employees
              </p>
            </div>
            <div className="rounded-lg border p-4">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <ClipboardList className="h-4 w-4" />
                Timesheet Status
              </div>
              <p className="mt-2 text-2xl font-semibold">
                {pendingCount ?? statistics?.pendingTimesheetsCount ?? 0} pending
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ManagerStatisticsPage;

