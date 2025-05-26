import {
  Box, Container, Typography, List, ListItem, ListItemText,
  Button, Divider
} from '@mui/material';
import { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

interface User {
  id: number;
  name: string;
  email: string;
}

export default function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [friendIds, setFriendIds] = useState<Set<number>>(new Set());
  const navigate = useNavigate();

  const currentUser = JSON.parse(localStorage.getItem('user') || '{}') as User;

  useEffect(() => {
    axios.get('/api/users').then(res => setUsers(res.data));
    axios.get(`/api/friends/${currentUser.id}/all`)
        .then(res => setFriendIds(new Set(res.data.map((u: User) => u.id))));
  }, []);

  const addFriend = async (friendId: number) => {
    await axios.post(`/api/friends/${currentUser.id}/add/${friendId}`);
    setFriendIds(prev => new Set(prev).add(friendId));
  };

  const removeFriend = async (friendId: number) => {
    await axios.delete(`/api/friends/${currentUser.id}/remove/${friendId}`);
    setFriendIds(prev => {
      const newSet = new Set(prev);
      newSet.delete(friendId);
      return newSet;
    });
  };

  return (
      <Container sx={{ mt: 4 }}>
        <Typography variant="h4" gutterBottom>Пользователи</Typography>
        <List>
          {users
              .filter(user => user.id !== currentUser.id)
              .map(user => (
                  <Box key={user.id}>
                    <ListItem
                        secondaryAction={
                          friendIds.has(user.id) ? (
                              <Button
                                  variant="outlined"
                                  color="error"
                                  onClick={() => removeFriend(user.id)}
                              >
                                Удалить из друзей
                              </Button>
                          ) : (
                              <Button
                                  variant="contained"
                                  onClick={() => addFriend(user.id)}
                              >
                                Добавить в друзья
                              </Button>
                          )
                        }
                        onClick={() => navigate(`/profile/${user.id}`)}
                    >
                      <ListItemText primary={user.name} secondary={user.email} />
                    </ListItem>
                    <Divider />
                  </Box>
              ))}
        </List>
      </Container>
  );
}