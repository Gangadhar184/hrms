import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { PageLoading } from '@/components/common';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const { isAuthenticated, isLoading, user } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <PageLoading text="Checking authentication..." />;
  }

  if (!isAuthenticated) {
    // Redirect to login with the return url
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // If user needs to reset password and not already on reset page
  if (user?.isFirstLogin && location.pathname !== '/reset-password') {
    return <Navigate to="/reset-password" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;

