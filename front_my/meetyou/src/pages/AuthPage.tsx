import {
  Container, Box, TextField, Button, Typography, Paper, Tabs, Tab
} from '@mui/material';
import { useState } from 'react';
import axios from 'axios';

export default function AuthPage() {
  const [tab, setTab] = useState(0); // 0 — Вход, 1 — Регистрация
  const [formData, setFormData] = useState({ name: '', email: '' });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async () => {
    try {
      const url = tab === 0 ? '/api/users' : '/api/users';
      const method = tab === 0 ? 'get' : 'post';

      if (tab === 0) {
        const response = await axios.get(`/api/users`);
        const foundUser = response.data.find(
          (u: any) => u.email === formData.email
        );
        if (foundUser) {
          localStorage.setItem('user', JSON.stringify(foundUser));
          window.location.href = `/profile/${foundUser.id}`;
        } else {
          alert('Пользователь не найден');
        }
      } else {
        const response = await axios.post(url, formData);
        localStorage.setItem('user', JSON.stringify(response.data));
        window.location.href = `/profile/${response.data.id}`;
      }
    } catch (err) {
      console.error(err);
      alert('Ошибка запроса');
    }
  };

  return (
    <Container maxWidth="sm" sx={{ mt: 6 }}>
      <Paper elevation={4} sx={{ p: 4 }}>
        <Tabs value={tab} onChange={(e, val) => setTab(val)} centered>
          <Tab label="Вход" />
          <Tab label="Регистрация" />
        </Tabs>
        <Box display="flex" flexDirection="column" gap={2} mt={3}>
          {tab === 1 && (
            <TextField
              name="name"
              label="Имя"
              value={formData.name}
              onChange={handleChange}
              fullWidth
            />
          )}
          <TextField
            name="email"
            label="Email"
            value={formData.email}
            onChange={handleChange}
            fullWidth
          />
          <Button variant="contained" onClick={handleSubmit}>
            {tab === 0 ? 'Войти' : 'Зарегистрироваться'}
          </Button>
        </Box>
      </Paper>
    </Container>
  );
}
