// src/pages/LogoutPage.tsx
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

export default function LogoutPage() {
  const navigate = useNavigate();

  useEffect(() => {
    localStorage.removeItem('user');
    navigate('/auth', { replace: true });
  }, [navigate]);

  return null;
}
