import { usePayInfo } from '@/hooks/useEmployee';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { PageError } from '@/components/common';
import { DollarSign, Calendar, CreditCard } from 'lucide-react';

const PayInfoSkeleton = () => (
  <Card>
    <CardHeader>
      <Skeleton className="h-6 w-48" />
      <Skeleton className="mt-2 h-4 w-64" />
    </CardHeader>
    <CardContent className="space-y-4">
      {[1, 2, 3, 4, 5].map((i) => (
        <div key={i} className="flex justify-between">
          <Skeleton className="h-4 w-32" />
          <Skeleton className="h-4 w-24" />
        </div>
      ))}
    </CardContent>
  </Card>
);

export const PayInfoPage = () => {
  const { data: payInfo, isLoading, isError, error, refetch } = usePayInfo();

  if (isLoading) return <PayInfoSkeleton />;

  if (isError) {
    return (
      <PageError
        title="Failed to load pay info"
        message={error?.response?.data?.message || 'Unable to load pay information.'}
        onRetry={() => refetch()}
      />
    );
  }

  if (!payInfo) return null;

  const formatCurrency = (amount?: number) => {
    if (amount === undefined || amount === null) return 'N/A';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const getPayFrequencyLabel = (frequency: string) => {
    const labels: Record<string, string> = {
      WEEKLY: 'Weekly',
      BI_WEEKLY: 'Bi-Weekly',
      SEMI_MONTHLY: 'Semi-Monthly',
      MONTHLY: 'Monthly',
    };
    return labels[frequency] || frequency;
  };

  const getPaymentMethodLabel = (method: string) => {
    const labels: Record<string, string> = {
      BANK_TRANSFER: 'Bank Transfer',
      CHECK: 'Check',
      CASH: 'Cash',
      DIGITAL_WALLET: 'Digital Wallet',
    };
    return labels[method] || method;
  };

  return (
    <div className="grid gap-6 md:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <DollarSign className="h-5 w-5" /> Compensation
          </CardTitle>
          <CardDescription>Your salary and pay rate information</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-muted-foreground">Annual Salary</span>
            <span className="font-semibold text-lg">{formatCurrency(payInfo.salary)}</span>
          </div>
          {payInfo.hourlyRate && (
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Hourly Rate</span>
              <span className="font-medium">{formatCurrency(payInfo.hourlyRate)}/hr</span>
            </div>
          )}
          <div className="flex items-center justify-between">
            <span className="text-muted-foreground">Pay Frequency</span>
            <Badge variant="outline">{getPayFrequencyLabel(payInfo.payFrequency)}</Badge>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <CreditCard className="h-5 w-5" /> Payment Method
          </CardTitle>
          <CardDescription>How you receive your payments</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-muted-foreground">Method</span>
            <Badge>{getPaymentMethodLabel(payInfo.paymentMethod)}</Badge>
          </div>
          {payInfo.bankName && (
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Bank</span>
              <span className="font-medium">{payInfo.bankName}</span>
            </div>
          )}
          {payInfo.maskedAccountNumber && (
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Account</span>
              <span className="font-mono">{payInfo.maskedAccountNumber}</span>
            </div>
          )}
        </CardContent>
      </Card>

      <Card className="md:col-span-2">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Calendar className="h-5 w-5" /> Pay Schedule
          </CardTitle>
          <CardDescription>Your upcoming and past payment dates</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2">
            <div className="rounded-lg border p-4">
              <p className="text-sm text-muted-foreground">Last Pay Date</p>
              <p className="mt-1 text-lg font-semibold">
                {formatDate(payInfo.lastPayDate)}
              </p>
            </div>
            <div className="rounded-lg border p-4 bg-primary/5">
              <p className="text-sm text-muted-foreground">Next Pay Date</p>
              <p className="mt-1 text-lg font-semibold text-primary">
                {formatDate(payInfo.nextPayDate)}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default PayInfoPage;

