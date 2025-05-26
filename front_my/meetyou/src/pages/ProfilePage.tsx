import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  Box,
  Typography,
  Container,
  Avatar,
  Divider,
  IconButton,
  Grid,
  Button,
} from '@mui/material';
import axios from 'axios';
import InterestsSection from '../components/InterestsSection';
import FriendsSection from '../components/FriendsSection';
import PhotosSection from '../components/PhotosSection';
import BioSection from '../components/BioSection';

interface User {
  id: number;
  name: string;
  email: string;
}

export default function ProfilePage() {
  const { id } = useParams();
  const [user, setUser] = useState<User | null>(null);
  const currentUser = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    axios.get(`/api/users/${id}`).then(res => setUser(res.data));
  }, [id]);

  if (!user) return null;

  return (
    <Container sx={{ mt: 4 }}>
      <Box display="flex" alignItems="center" gap={2}>
        <Avatar sx={{ width: 64, height: 64 }}>{user.name[0]}</Avatar>
        <Box>
          <Typography variant="h5">{user.name}</Typography>
          <Typography variant="body1" color="text.secondary">
            {user.email}
          </Typography>
        </Box>
      </Box>

      <Divider sx={{ my: 3 }} />

      <Box mb={4}>
        <BioSection userId={user.id} isOwner={currentUser.id === user.id} />
      </Box>

      <Box
        display="grid"
        gap={4}
        gridTemplateColumns={{ xs: '1fr', md: '1fr 1fr' }}
      >
        <Box>
          <InterestsSection userId={user.id} />
        </Box>
        <Box>
          <FriendsSection userId={user.id} />
        </Box>
        <Box gridColumn="1 / -1">
          <PhotosSection userId={user.id} />
        </Box>
      </Box>
    </Container>
  );
}
