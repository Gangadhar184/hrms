import { useState } from 'react';
import { usePayrollHistory, useMarkPayrollAsPaid } from '@/hooks/useAdmin';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { PageError, EmptyState } from '@/components/common';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { DollarSign, Calendar, CheckCircle, Loader2 } from 'lucide-react';
import { PayrollStatus } from '@/types';

const getStatusBadge = (status: PayrollStatus) => {
  switch (status) {
    case PayrollStatus.PREVIEW:
      return <Badge variant="secondary">Preview</Badge>;
    case PayrollStatus.PROCESSED:
      return <Badge variant="warning">Processed</Badge>;
    case PayrollStatus.PAID:
      return <Badge variant="success">Paid</Badge>;
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
};

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
};

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
};

export const AdminPayrollHistoryPage = () => {
  const [startDate, setStartDate] = useState(() => {
    const date = new Date();
    date.setMonth(date.getMonth() - 3);
    return date.toISOString().split('T')[0];
  });
  const [endDate, setEndDate] = useState(() => {
    return new Date().toISOString().split('T')[0];
  });

  const { data: payrolls, isLoading, isError, error, refetch } = usePayrollHistory(startDate, endDate);
  const markPaidMutation = useMarkPayrollAsPaid();

  const handleMarkAsPaid = (payrollId: number) => {
    markPaidMutation.mutate(payrollId);
  };

  return (
    <div className="space-y-6">
      {/* Date Range Filter */}
      <Card>
        <CardContent className="flex flex-col gap-4 p-4 md:flex-row md:items-end">
          <div className="flex-1 space-y-2">
            <Label htmlFor="startDate">Start Date</Label>
            <Input
              id="startDate"
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
            />
          </div>
          <div className="flex-1 space-y-2">
            <Label htmlFor="endDate">End Date</Label>
            <Input
              id="endDate"
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
            />
          </div>
          <Button variant="outline" onClick={() => refetch()}>
            <Calendar className="mr-2 h-4 w-4" />
            Filter
          </Button>
        </CardContent>
      </Card>

      {/* Payroll History Table */}
      {isLoading ? (
        <Card>
          <CardContent className="p-4">
            <div className="space-y-4">
              {[1, 2, 3, 4, 5].map((i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          </CardContent>
        </Card>
      ) : isError ? (
        <PageError
          title="Failed to load payroll history"
          message={error?.response?.data?.message || 'Unable to load payroll history.'}
          onRetry={() => refetch()}
        />
      ) : !payrolls || payrolls.length === 0 ? (
        <EmptyState
          icon={DollarSign}
          title="No payroll records"
          description="No payroll records found for the selected date range."
        />
      ) : (
        <Card>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Employee</TableHead>
                  <TableHead>Pay Period</TableHead>
                  <TableHead className="text-right">Gross Pay</TableHead>
                  <TableHead className="text-right">Net Pay</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Payment Date</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {payrolls.map((payroll) => (
                  <TableRow key={payroll.id}>
                    <TableCell className="font-medium">
                      {payroll.employee.name}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formatDate(payroll.payPeriodStart)} - {formatDate(payroll.payPeriodEnd)}
                    </TableCell>
                    <TableCell className="text-right">
                      {formatCurrency(payroll.grossPay)}
                    </TableCell>
                    <TableCell className="text-right font-semibold">
                      {formatCurrency(payroll.netPay)}
                    </TableCell>
                    <TableCell>{getStatusBadge(payroll.status)}</TableCell>
                    <TableCell className="text-muted-foreground">
                      {payroll.paymentDate ? formatDate(payroll.paymentDate) : '-'}
                    </TableCell>
                    <TableCell className="text-right">
                      {payroll.status === PayrollStatus.PROCESSED && (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleMarkAsPaid(payroll.id)}
                          disabled={markPaidMutation.isPending}
                        >
                          {markPaidMutation.isPending ? (
                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          ) : (
                            <CheckCircle className="mr-2 h-4 w-4" />
                          )}
                          Mark Paid
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default AdminPayrollHistoryPage;

