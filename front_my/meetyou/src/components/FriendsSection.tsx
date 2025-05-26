// src/components/FriendsSection.tsx
import {
  Box, Typography, List, ListItem, ListItemText, IconButton
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/PersonRemove';
import { useEffect, useState } from 'react';
import axios from 'axios';

interface Props {
  userId: number;
}

interface Friend {
  id: number;
  name: string;
  email: string;
}

export default function FriendsSection({ userId }: Props) {
  const [friends, setFriends] = useState<Friend[]>([]);

  const loadFriends = () => {
    axios.get(`/api/friends/${userId}/all`).then(res => setFriends(res.data));
  };

  useEffect(() => {
    loadFriends();
  }, []);

  const removeFriend = async (friendId: number) => {
    await axios.delete(`/api/friends/${userId}/remove/${friendId}`);
    loadFriends();
  };

  return (
    <Box>
      <Typography variant="h6" mb={2}>Друзья</Typography>
      <List>
        {friends.map(friend => (
          <ListItem
            key={friend.id}
            secondaryAction={
              <IconButton edge="end" onClick={() => removeFriend(friend.id)}>
                <DeleteIcon />
              </IconButton>
            }
          >
            <ListItemText
              primary={friend.name}
              secondary={friend.email}
            />
          </ListItem>
        ))}
      </List>
    </Box>
  );
}
