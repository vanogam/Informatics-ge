import {TextField, Box, FormGroup, FormControlLabel, Checkbox, MenuItem, Stack, Button} from '@mui/material'
import {DateTimePicker} from '@mui/x-date-pickers/DateTimePicker'
import getMessage from "../../Components/lang";

// Kept in step with ScoringType, which is persisted by ordinal: constants are only ever appended.
const scoringTypes = ['BEST_SUBMISSION', 'LAST_SUBMISSION', 'SUBTASK_MAX']

export default function ContestForm({contestData, setContestData, handleSubmit, buttonText}) {
    const handleChange = (event) => {
        setContestData((prevData) => ({...prevData, archive: event.target.checked}))
    }

    const handleChange2 = (event) => {
        setContestData((prevData) => ({...prevData, autoArchive: event.target.checked}))
    }

    return (
        <Stack gap='1rem' maxWidth='25rem' mx='auto'>
            <TextField
                label='Contest Name'
                value={contestData.contestName}
                onChange={(e) =>
                    setContestData((prevData) => ({
                        ...prevData,
                        contestName: e.target.value,
                    }))
                }
                variant='outlined'
            />
            <Box sx={{display: 'flex', flexDirection: 'row'}}>
                <FormGroup>
                    <FormControlLabel
                        control={
                            <Checkbox
                                checked={contestData.archive}
                                onChange={handleChange}
                                inputProps={{'aria-label': 'controlled'}}
                            />
                        }
                        label='დაარქივება'
                    />
                </FormGroup>
                <FormGroup>
                    <FormControlLabel
                        control={
                            <Checkbox
                                checked={contestData.autoArchive}
                                onChange={handleChange2}
                                inputProps={{'aria-label': 'controlled'}}
                            />
                        }
                        label='ავტომატური დაარქივება'
                    />
                </FormGroup>
            </Box>
            <DateTimePicker
                label={getMessage('ka', 'startDate')}
                value={contestData.startDate || null}
                onChange={(date) =>
                    setContestData((prevData) => ({...prevData, startDate: date || null}))
                }
                inputFormat={'DD/MM/YYYY HH:mm'}
                renderInput={(params) => <TextField variant='outlined' {...params} error={!contestData.startDate}/>}
            />
            <TextField
                select
                label={getMessage('ka', 'scoringType')}
                value={contestData.scoringType || 'BEST_SUBMISSION'}
                onChange={(e) =>
                    setContestData((prevData) => ({...prevData, scoringType: e.target.value}))
                }
                variant='outlined'
            >
                {scoringTypes.map((type) => (
                    <MenuItem key={type} value={type}>
                        {getMessage('ka', `SCORING_TYPE_${type}`)}
                    </MenuItem>
                ))}
            </TextField>
            <Stack direction='row' gap='1rem'>
                <TextField
                    label='ხანგრძლივობა (წთ)'
                    variant='outlined'
                    type='number'
                    value={contestData.duration}
                    onChange={(e) =>
                        setContestData((prevData) => ({
                            ...prevData,
                            duration: e.target.value,
                        }))
                    }
                    fullWidth
                />
            </Stack>
            <Button onClick={handleSubmit} variant='contained' sx={{background: '#3c324e'}} size='large'>
                {buttonText}
            </Button>
        </Stack>
    )
}