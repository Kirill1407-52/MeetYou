import { Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import AuthPage from './pages/AuthPage';
import ProfilePage from './pages/ProfilePage';
import UsersPage from './pages/UsersPage';
import DialogsPage from './pages/DialogsPage';
import MessagesPage from './pages/MessagesPage';
import AppLayout from './layout/AppLayout';
import ProtectedRoute from './components/ProtectedRoute';
import LogoutPage from './pages/LogoutPage';

export default function App() {
  return (
    <Routes>
      {/* Страница авторизации */}
      <Route path="/auth" element={<AuthPage />} />

      {/* Защищённые маршруты */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="profile/:id" element={<ProfilePage />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="dialogs" element={<DialogsPage />} />
        <Route path="messages/:userId" element={<MessagesPage />} />
        <Route path="logout" element={<LogoutPage />} />
        <Route index element={<HomePage />} />
      </Route>
    </Routes>
  );
}
