import {
  Box, Typography, Grid, IconButton, Button, ImageList, ImageListItem, Tooltip
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import StarIcon from '@mui/icons-material/Star';
import StarBorderIcon from '@mui/icons-material/StarBorder';
import { useEffect, useState } from 'react';
import axios from 'axios';

interface Props {
  userId: number;
}

interface Photo {
  id: number;
  url: string;
  isMain: boolean;
}

export default function PhotosSection({ userId }: Props) {
  const [photos, setPhotos] = useState<Photo[]>([]);
  const [files, setFiles] = useState<FileList | null>(null);

  const fetchPhotos = () => {
    axios.get(`/api/users/${userId}/photos`).then(res => setPhotos(res.data));
  };

  useEffect(() => {
    fetchPhotos();
  }, []);

  const uploadPhotos = async () => {
    if (!files || files.length === 0) return;
    const formData = new FormData();
    for (let i = 0; i < files.length; i++) {
      formData.append('files', files[i]);
    }
    await axios.post(`/api/users/${userId}/photos/batch`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    setFiles(null);
    fetchPhotos();
  };

  const deletePhoto = async (photoId: number) => {
    await axios.delete(`/api/users/${userId}/photos/${photoId}`);
    fetchPhotos();
  };

  const setAsMain = async (photoId: number) => {
    await axios.put(`/api/users/${userId}/photos/${photoId}/set-main`);
    fetchPhotos();
  };

  return (
    <Box>
      <Typography variant="h6" mb={2}>Фотографии</Typography>
<Box display="flex" gap={2} mt={2}>
  <Box>
    <input
      type="file"
      accept="image/*"
      multiple
      onChange={(e) => setFiles(e.target.files)}
    />
  </Box>
  <Box>
    <Button
      variant="contained"
      onClick={uploadPhotos}
      disabled={!files}
    >
      Загрузить
    </Button>
  </Box>
</Box>



      <ImageList cols={3} gap={8} sx={{ mt: 3 }}>
        {photos.map((photo) => (
          <ImageListItem key={photo.id}>
            <img
              src={photo.url}
              alt={`Фото ${photo.id}`}
              loading="lazy"
              style={{ borderRadius: '8px' }}
            />
            <Box
              sx={{
                position: 'absolute',
                top: 4,
                right: 4,
                display: 'flex',
                gap: 1
              }}
            >
              <Tooltip title="Удалить">
                <IconButton
                  onClick={() => deletePhoto(photo.id)}
                  sx={{ bgcolor: 'rgba(255,255,255,0.7)' }}
                >
                  <DeleteIcon fontSize="small" />
                </IconButton>
              </Tooltip>
              <Tooltip title={photo.isMain ? "Основное фото" : "Сделать основным"}>
                <IconButton
                  onClick={() => !photo.isMain && setAsMain(photo.id)}
                  sx={{ bgcolor: 'rgba(255,255,255,0.7)' }}
                >
                  {photo.isMain ? (
                    <StarIcon color="primary" fontSize="small" />
                  ) : (
                    <StarBorderIcon fontSize="small" />
                  )}
                </IconButton>
              </Tooltip>
            </Box>
          </ImageListItem>
        ))}
      </ImageList>
    </Box>
  );
}
