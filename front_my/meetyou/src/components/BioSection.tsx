import {

    Box, Typography, TextField, Button, Stack

} from '@mui/material';

import { useEffect, useState } from 'react';

import axios from 'axios';



interface Props {

    userId: number;

    isOwner: boolean;

}



export default function BioSection({ userId, isOwner }: Props) {

    const [bio, setBio] = useState('');

    const [fact, setFact] = useState('');

    const [bioInput, setBioInput] = useState('');

    const [factInput, setFactInput] = useState('');

    const [bioExists, setBioExists] = useState(false);

    const [factExists, setFactExists] = useState(false);



    useEffect(() => {

        axios.get(`/api/users/${userId}/bioall`)

            .then(res => {

                const { bioText, interestFact } = res.data;

                setBio(bioText);

                setFact(interestFact);

                setBioInput(bioText);

                setFactInput(interestFact);

                setBioExists(!!bioText);

                setFactExists(!!interestFact);

            })

            .catch(() => {

                setBio('');

                setFact('');

                setBioInput('');

                setFactInput('');

                setBioExists(false);

                setFactExists(false);

            });

    }, [userId]);



    const updateBio = async () => {

        console.log('Отправка био:', { bioText: bioInput });

        const method = bioExists ? 'put' : 'post';

        await axios({

            method,

            url: `/api/users/${userId}/bio`,

            data: { bioText: bioInput },

        });

        setBio(bioInput);

        setBioExists(true);

    };



    const updateFact = async () => {

        console.log('Отправка факта:', { interestFact: factInput });

        const method = factExists ? 'put' : 'post';

        await axios({

            method,

            url: `/api/users/${userId}/fact`,

            data: { interestFact: factInput },

        });

        setFact(factInput);

        setFactExists(true);

    };



    return (

        <Box>

            <Typography variant="h6" mb={2}>О себе</Typography>



            {isOwner ? (

                <Stack spacing={2}>

                    <TextField

                        label="Биография"

                        value={bioInput}

                        onChange={e => setBioInput(e.target.value)}

                        multiline

                        fullWidth

                    />

                    <Button variant="contained" onClick={updateBio}>

                        {bioExists ? 'Обновить биографию' : 'Создать биографию'}

                    </Button>



                    <TextField

                        label="Интересный факт"

                        value={factInput}

                        onChange={e => setFactInput(e.target.value)}

                        multiline

                        fullWidth

                    />

                    <Button variant="contained" onClick={updateFact}>

                        {factExists ? 'Обновить факт' : 'Создать факт'}

                    </Button>

                </Stack>

            ) : (

                <>

                    <Typography variant="body1" mb={2}><strong>Биография:</strong> {bio || '—'}</Typography>

                    <Typography variant="body1"><strong>Факт:</strong> {fact || '—'}</Typography>

                </>

            )}

        </Box>

    );

}