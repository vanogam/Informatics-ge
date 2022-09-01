import dayjs, { Dayjs } from 'dayjs'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import {
	Button,
	Container,
	MenuItem,
	Paper,
	Stack,
	TextField,
	Typography,
} from '@mui/material'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import { useRef, useState } from 'react'
import axios from 'axios'
import NewTaskCard from './NewTaskCard'

export default function NewContest() {
	// DELETE ME
	const [contestId, setContestId] = useState(null)
	//
	//

	const [value, setValue] = useState(dayjs('2014-08-18T21:11:54'))
	const [durationType, setDurationType] = useState('Minutes')
	const nameRef = useRef(null)
	const durationRef = useRef(null)

	const durationTypes = ['Hours', 'Minutes']

	const handleAddContest = () => {
		const params = {
			name: nameRef?.current.value,
			startDate: value.format('DD/MM/YYYY HH:mm'),
			durationInSeconds:
				durationType === 'Minutes'
					? durationRef?.current.value * 60
					: durationRef?.current.value * 3600,
			roomId: 1,
		}
		setContestId(5)
		// axios
		// 	.post(endpoint, params)
		// 	.then((res) => setContestId(res.data.contestId))
		console.log(params)
	}

	return (
		<LocalizationProvider dateAdapter={AdapterDayjs}>
			<Container maxWidth="xs">
				<Stack gap="1rem" marginTop="2rem">
					<Paper elevation={4} sx={{ padding: '1rem' }}>
						{!contestId ? (
							<>
								<Typography variant="h4" align="center" pb="1rem">
									New Contest
								</Typography>
								<Stack gap="1rem" maxWidth="25rem" mx="auto">
									<TextField
										label="Contest Name"
										inputRef={nameRef}
										variant="outlined"
									/>
									<DateTimePicker
										label="Start Date"
										value={value}
										onChange={setValue}
										renderInput={(params) => (
											<TextField variant="outlined" {...params} />
										)}
									/>
									<Stack direction="row" gap="1rem">
										<TextField
											label="Contest Duration (seconds)"
											variant="outlined"
											type="number"
											inputRef={durationRef}
											fullWidth
										/>
										<TextField
											select
											// label="Select"
											value={durationType}
											onChange={(e) => {
												setDurationType(e.target.value)
											}}
											sx={{ minWidth: 'max-content' }}
										>
											{durationTypes.map((option) => (
												<MenuItem key={option} value={option}>
													{option}
												</MenuItem>
											))}
										</TextField>
									</Stack>
									<Button
										onClick={handleAddContest}
										variant="contained"
										color="success"
										size="large"
									>
										Add Contest
									</Button>
								</Stack>
							</>
						) : (
							<>
								<Typography variant="h4" align="center">
									{nameRef?.current?.value}
								</Typography>
								<Typography>ContestID: {contestId}</Typography>
							</>
						)}
					</Paper>
					{contestId && (
						<Stack>
							<NewTaskCard />
						</Stack>
					)}
				</Stack>
			</Container>
		</LocalizationProvider>
	)
}