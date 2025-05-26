import {
  Box, Container, Typography, TextField, Button, Stack, Paper
} from '@mui/material';
import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

interface Message {
  id: number;
  senderId: number;
  receiverId: number;
  content: string;
  timestamp: string;
  isRead: boolean;
}

export default function MessagesPage() {
  const { userId } = useParams();
  const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
  const [messages, setMessages] = useState<Message[]>([]);
  const [text, setText] = useState('');
  const scrollRef = useRef<HTMLDivElement>(null);

  const fetchMessages = async () => {
    const res = await axios.get('/api/messages/conversation', {
      params: {
        user1Id: currentUser.id,
        user2Id: userId,
      }
    });
    setMessages(res.data);

    await axios.post('/api/messages/mark-as-read', null, {
      params: {
        userId: currentUser.id,
        interlocutorId: userId,
      }
    });
  };

  useEffect(() => {
    fetchMessages();
    const interval = setInterval(fetchMessages, 5000); // ← опрос каждые 5 секунд
    return () => clearInterval(interval);
  }, [userId]);

  useEffect(() => {
    scrollRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = async () => {
    if (!text.trim()) return;

    await axios.post('/api/messages', null, {
      params: {
        senderId: currentUser.id,
        receiverId: userId,
        content: text,
      }
    });

    setText('');
    fetchMessages(); // обновить после отправки
  };

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h5" mb={2}>
        Чат с пользователем #{userId}
      </Typography>

      <Paper
        sx={{
          p: 2,
          maxHeight: '60vh',
          overflowY: 'auto',
          display: 'flex',
          flexDirection: 'column',
          gap: 1,
        }}
      >
        {messages.map((msg) => (
          <Box
            key={msg.id}
            alignSelf={msg.senderId === currentUser.id ? 'flex-end' : 'flex-start'}
            sx={{
              bgcolor: msg.senderId === currentUser.id ? 'primary.main' : 'grey.300',
              color: msg.senderId === currentUser.id ? 'white' : 'black',
              px: 2,
              py: 1,
              borderRadius: 2,
              maxWidth: '70%',
            }}
          >
            {msg.content}
            <Typography variant="caption" display="block" align="right" sx={{ opacity: 0.7 }}>
              {new Date(msg.timestamp).toLocaleTimeString()}
            </Typography>
          </Box>
        ))}
        <div ref={scrollRef} />
      </Paper>

      <Box mt={2} display="flex" gap={2}>
        <TextField
          fullWidth
          autoFocus
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && sendMessage()}
          placeholder="Введите сообщение..."
        />
        <Button onClick={sendMessage} variant="contained">Отправить</Button>
      </Box>
    </Container>
  );
}
