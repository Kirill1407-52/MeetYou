// src/components/InterestsSection.tsx
import {
  Box, Typography, TextField, Button, Chip, Stack
} from '@mui/material';
import { useEffect, useState } from 'react';
import axios from 'axios';

interface Props {
  userId: number;
}

export default function InterestsSection({ userId }: Props) {
  const [interests, setInterests] = useState<string[]>([]);
  const [newInterest, setNewInterest] = useState('');

  const fetchInterests = () =>
    axios.get(`/api/users/${userId}/interests`)
      .then(res => setInterests(res.data.map((i: any) => i.name)));

  useEffect(() => {
    fetchInterests();
  }, []);

  const addInterest = async () => {
    if (!newInterest.trim()) return;
    await axios.post(`/api/users/${userId}/interests`, null, {
      params: { interestName: newInterest }
    });
    setNewInterest('');
    fetchInterests();
  };

  const removeInterest = async (name: string) => {
    await axios.delete(`/api/users/${userId}/interests`, {
      params: { interestName: name }
    });
    fetchInterests();
  };

  return (
    <Box>
      <Typography variant="h6" mb={2}>Интересы</Typography>
      <Stack direction="row" gap={1} mb={2} flexWrap="wrap">
        {interests.map((name, index) => (
          <Chip
            key={index}
            label={name}
            onDelete={() => removeInterest(name)}
            color="primary"
          />
        ))}
      </Stack>
      <TextField
        label="Новый интерес"
        value={newInterest}
        onChange={e => setNewInterest(e.target.value)}
        size="small"
      />
      <Button onClick={addInterest} sx={{ ml: 2 }} variant="contained">
        Добавить
      </Button>
    </Box>
  );
}
