import { useParams, Link } from 'react-router-dom';
import { useEmployeePayrollById } from '@/hooks/usePayrollEmployee';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Separator } from '@/components/ui/separator';
import { PageError } from '@/components/common';
import { ArrowLeft, Calendar, DollarSign, Minus, Plus, CreditCard } from 'lucide-react';
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
            <Skeleton key={i} className="h-8 w-full" />
          ))}
        </div>
      </CardContent>
    </Card>
  </div>
);

export const PayrollDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const payrollId = id ? parseInt(id) : undefined;
  const { data: payroll, isLoading, isError, error, refetch } = useEmployeePayrollById(payrollId);

  if (isLoading) return <DetailSkeleton />;

  if (isError) {
    return (
      <PageError
        title="Failed to load payroll"
        message={error?.response?.data?.message || 'Unable to load payroll details.'}
        onRetry={() => refetch()}
      />
    );
  }

  if (!payroll) return null;

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'long',
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
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" asChild>
          <Link to="/payroll">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to History
          </Link>
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <CreditCard className="h-5 w-5" />
                Payslip
              </CardTitle>
              <CardDescription>
                Pay Period: {formatDate(payroll.payPeriodStart)} - {formatDate(payroll.payPeriodEnd)}
              </CardDescription>
            </div>
            {getStatusBadge(payroll.status)}
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Earnings Section */}
          <div>
            <h3 className="mb-3 flex items-center gap-2 font-semibold text-green-600">
              <Plus className="h-4 w-4" /> Earnings
            </h3>
            <div className="space-y-2 rounded-lg border p-4">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Gross Pay</span>
                <span className="font-medium">{formatCurrency(payroll.grossPay)}</span>
              </div>
              {payroll.bonus > 0 && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Bonus</span>
                  <span className="font-medium text-green-600">+{formatCurrency(payroll.bonus)}</span>
                </div>
              )}
            </div>
          </div>

          {/* Deductions Section */}
          <div>
            <h3 className="mb-3 flex items-center gap-2 font-semibold text-red-600">
              <Minus className="h-4 w-4" /> Deductions
            </h3>
            <div className="space-y-2 rounded-lg border p-4">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Tax Deduction</span>
                <span className="font-medium text-red-600">-{formatCurrency(payroll.taxDeduction)}</span>
              </div>
              {payroll.otherDeductions > 0 && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Other Deductions</span>
                  <span className="font-medium text-red-600">-{formatCurrency(payroll.otherDeductions)}</span>
                </div>
              )}
              <Separator />
              <div className="flex justify-between font-medium">
                <span>Total Deductions</span>
                <span className="text-red-600">-{formatCurrency(payroll.taxDeduction + payroll.otherDeductions)}</span>
              </div>
            </div>
          </div>

          {/* Net Pay */}
          <div className="rounded-lg bg-primary/5 p-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <DollarSign className="h-6 w-6 text-primary" />
                <span className="text-lg font-semibold">Net Pay</span>
              </div>
              <span className="text-2xl font-bold text-primary">{formatCurrency(payroll.netPay)}</span>
            </div>
          </div>

          {/* Payment Info */}
          <div className="grid gap-4 rounded-lg border p-4 md:grid-cols-2">
            <div className="flex items-center gap-3">
              <Calendar className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Payment Date</p>
                <p className="font-medium">{payroll.paymentDate ? formatDate(payroll.paymentDate) : 'Pending'}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Calendar className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Processed At</p>
                <p className="font-medium">{payroll.processedAt ? formatDate(payroll.processedAt) : 'Pending'}</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default PayrollDetailPage;

