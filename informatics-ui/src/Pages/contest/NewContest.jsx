import dayjs from 'dayjs'
import {AdapterDayjs} from '@mui/x-date-pickers/AdapterDayjs'
import {LocalizationProvider} from '@mui/x-date-pickers/LocalizationProvider'
import {
    Button,
    Container,
    MenuItem,
    Paper,
    Stack,
    TextField,
    Typography,
} from '@mui/material'
import {DateTimePicker} from '@mui/x-date-pickers/DateTimePicker'
import {useContext, useRef, useState} from 'react'
import {NavLink} from 'react-router-dom'
import {AxiosContext, getAxiosInstance} from '../../utils/axiosInstance'
import getMessage from '../../Components/lang'

export default function NewContest() {
    const [contestId, setContestId] = useState(null)
    const [contestName, setContestName] = useState(null)
    const axiosInstance = useContext(AxiosContext)

    const [value, setValue] = useState(dayjs(new Date()))
    const [showError, setShowError] = useState(false)
    const [durationType, setDurationType] = useState('Minutes')
    const [tasks, setTasks] = useState([])
    const nameRef = useRef(null)
    const durationRef = useRef(null)
    const [scoringType, setScoringType] = useState('BEST_SUBMISSION');
    const [upsolvingAfterFinished, setUpsolvingAfterFinished] = useState(true);

    const scoringTypes = ['BEST_SUBMISSION', 'LAST_SUBMISSION'];

    const [showNewTaskCard, setShowNewTaskCard] = useState(false)
    const durationTypes = ['Hours', 'Minutes']


    const handleAddContest = () => {
        setShowError(true);
        if (!isValid()) {
            return;
        }
        const params = {
            name: nameRef?.current.value,
            startDate: value?.format('DD/MM/YYYY HH:mm'),
            durationInSeconds:
                !durationRef?.current.value
                    ? null
                    : durationType === 'Minutes'
                        ? durationRef?.current.value * 60
                        : durationRef?.current.value * 3600,
            roomId: "1",
            scoringType,
            upsolvingAfterFinished,
        };
        params["durationInSeconds"] = params["durationInSeconds"]?.toString();
        setContestName(nameRef?.current.value);
        axiosInstance
            .post('/create-contest', params)
            .then((res) => {
                setContestId(res.data.contest.id);
            });
    };

    const isValid = () => {
        console.log("!!!")
        return !!nameRef.current.value
            && (!!durationRef.current.value
                && !!value) || (
                !value && !durationRef.current.value
            )
    }

    const handleSubmit = (title) => {
        setTasks((prevState) => [...prevState, title])
        setShowNewTaskCard(false)
    }

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs}>
            <Container maxWidth="xs">
                <Stack gap="1rem" marginTop="2rem">
                    <Paper elevation={4} sx={{padding: '1rem'}}>

                        <Typography variant="h5" align="center" pb="1rem">
                            {getMessage('ka', 'addContest')}
                        </Typography>
                        <Stack gap="1rem" maxWidth="25rem" mx="auto">
                            <TextField
                                label={getMessage('ka', 'name')}
                                inputRef={nameRef}
                                required={true}
                                error={!nameRef.current?.value && showError}
                                variant="outlined"
                            />
                            <DateTimePicker
                                label={getMessage('ka', 'startDate')}
                                value={value}
                                onChange={setValue}
                                inputFormat={'DD/MM/YYYY HH:mm'}
                                renderInput={(params) => (
                                    <TextField variant="outlined" {...params} />
                                )}
                            />
                            <Stack direction="row" gap="1rem">
                                <TextField
                                    label={`${getMessage('ka', 'duration')} (${getMessage(
                                        'ka',
                                        durationType === 'Minutes' ? 'minuteShort' : 'hourShort'
                                    )})`}
                                    variant="outlined"
                                    required={true}
                                    type="number"
                                    inputRef={durationRef}
                                    fullWidth
                                />
                                <TextField
                                    select
                                    value={durationType}
                                    onChange={(e) => {
                                        setDurationType(e.target.value);
                                    }}
                                    sx={{minWidth: 'max-content'}}
                                >
                                    {durationTypes.map((option) => (
                                        <MenuItem key={option} value={option}>
                                            {getMessage('ka', 'DATEFORMAT_' + option)}
                                        </MenuItem>
                                    ))}
                                </TextField>
                            </Stack>
                            <TextField
                                select
                                label={getMessage('ka', 'scoringType')}
                                value={scoringType}
                                onChange={(e) => setScoringType(e.target.value)}
                                variant="outlined"
                            >
                                {scoringTypes.map((type) => (
                                    <MenuItem key={type} value={type}>
                                        {getMessage('ka', `SCORING_TYPE_${type}`)}
                                    </MenuItem>
                                ))}
                            </TextField>
                            <Stack direction="row" alignItems="center" gap="0.5rem">
                                <Typography>{getMessage('ka', 'upsolvingAfterFinished')}</Typography>
                                <input
                                    type="checkbox"
                                    checked={upsolvingAfterFinished}
                                    onChange={(e) => setUpsolvingAfterFinished(e.target.checked)}
                                />
                            </Stack>
                            <Button
                                onClick={handleAddContest}
                                variant="contained"
                                sx={{background: '#3c324e'}}
                                size="large"
                            >
                                {getMessage('ka', 'addContest')}
                            </Button>
                        </Stack>
                    </Paper>
                </Stack>
            </Container>
        </LocalizationProvider>
    );
}
