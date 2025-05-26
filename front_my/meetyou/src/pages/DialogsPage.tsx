import {
  Container, Typography, List, ListItem, ListItemText,
  Badge, Divider
} from '@mui/material';
import ListItemButton from '@mui/material/ListItemButton';
import { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

interface User {
  id: number;
  name: string;
  email: string;
}

export default function DialogsPage() {
  const [friends, setFriends] = useState<User[]>([]);
  const [unreadCounts, setUnreadCounts] = useState<Record<number, number>>({});
  const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
  const navigate = useNavigate();

  useEffect(() => {
    axios.get(`/api/friends/${currentUser.id}/all`).then(res => {
      const friendList: User[] = res.data;
      setFriends(friendList);

      friendList.forEach((friend) => {
        axios.get(`/api/messages/unread/count`, {
          params: { userId: currentUser.id }
        }).then(res => {
          setUnreadCounts(prev => ({
            ...prev,
            [friend.id]: res.data // ⚠️ если API возвращает общее число — заменить подход
          }));
        });
      });
    });
  }, []);

  return (
    <Container sx={{ mt: 4 }}>
      <Typography variant="h5" gutterBottom>Диалоги</Typography>
      <List>
        {friends.map(friend => (
          <div key={friend.id}>
            <ListItem disablePadding>
              <ListItemButton onClick={() => navigate(`/messages/${friend.id}`)}>
                <ListItemText
                  primary={
                    <Badge
                      badgeContent={unreadCounts[friend.id] || 0}
                      color="primary"
                      invisible={(unreadCounts[friend.id] || 0) === 0}
                    >
                      {friend.name}
                    </Badge>
                  }
                  secondary={friend.email}
                />
              </ListItemButton>
            </ListItem>
            <Divider />
          </div>
        ))}
      </List>
    </Container>
  );
}
