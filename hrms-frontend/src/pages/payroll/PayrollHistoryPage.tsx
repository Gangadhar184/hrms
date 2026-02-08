import { Link } from 'react-router-dom';
import { useEmployeePayrollHistory } from '@/hooks/usePayrollEmployee';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { PageError, EmptyState } from '@/components/common';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { DollarSign, Eye } from 'lucide-react';
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

const PayrollSkeleton = () => (
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

export const PayrollHistoryPage = () => {
  const { data: payrolls, isLoading, isError, error, refetch } = useEmployeePayrollHistory();

  if (isLoading) return <PayrollSkeleton />;

  if (isError) {
    return (
      <PageError
        title="Failed to load payroll history"
        message={error?.response?.data?.message || 'Unable to load payroll history.'}
        onRetry={() => refetch()}
      />
    );
  }

  if (!payrolls || payrolls.length === 0) {
    return (
      <EmptyState
        icon={DollarSign}
        title="No payroll history"
        description="You don't have any payroll records yet."
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

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  return (
    <Card>
      <CardContent className="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Pay Period</TableHead>
              <TableHead className="text-right">Gross Pay</TableHead>
              <TableHead className="text-right">Deductions</TableHead>
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
                  {formatDate(payroll.payPeriodStart)} - {formatDate(payroll.payPeriodEnd)}
                </TableCell>
                <TableCell className="text-right">
                  {formatCurrency(payroll.grossPay)}
                </TableCell>
                <TableCell className="text-right text-muted-foreground">
                  -{formatCurrency(payroll.taxDeduction + payroll.otherDeductions)}
                </TableCell>
                <TableCell className="text-right font-semibold text-primary">
                  {formatCurrency(payroll.netPay)}
                </TableCell>
                <TableCell>{getStatusBadge(payroll.status)}</TableCell>
                <TableCell className="text-muted-foreground">
                  {payroll.paymentDate ? formatDate(payroll.paymentDate) : '-'}
                </TableCell>
                <TableCell className="text-right">
                  <Button variant="ghost" size="sm" asChild>
                    <Link to={`/payroll/${payroll.id}`}>
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

export default PayrollHistoryPage;

