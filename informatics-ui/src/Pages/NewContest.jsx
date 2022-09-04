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
	const [contestId, setContestId] = useState(null)
	const [contestName, setContestName] = useState(null)

	const [value, setValue] = useState(dayjs('2014-08-18T21:11:54'))
	const [durationType, setDurationType] = useState('Minutes')
	const [tasks, setTasks] = useState([])
	const nameRef = useRef(null)
	const durationRef = useRef(null)
	const [showNewTaskCard, setShowNewTaskCard] = useState(false)
	const durationTypes = ['Hours', 'Minutes']

	const handleAddContest = () => {
		console.log("HI")
		const params = {
			name: nameRef?.current.value,
			startDate: value.format('DD/MM/YYYY HH:mm'),
			durationInSeconds:
				durationType === 'Minutes'
					? durationRef?.current.value * 60
					: durationRef?.current.value * 3600,
			roomId: "1",
		}
		params["durationInSeconds"] = params["durationInSeconds"].toString()
		console.log(params)
		setContestName(nameRef?.current.value)
		axios
			.post('http://localhost:8080/create-contest', params)
			.then((res) => {setContestId(res.data.contest.id); 	axios.post(`http://localhost:8080/contest/${res.data.contest.id}/register`,{})})
	

	}

	const handleSubmit = (title) => {
		setTasks((prevState) => [...prevState, title])
		setShowNewTaskCard(false)
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
										sx = {{background: '#3c324e'}}
										size="large"
									>
										Add Contest
									</Button>
								</Stack>
							</>
						) : (
							<>
								<Typography variant="h4" align="center">
									{contestName}
								</Typography>
							</>
						)}
					</Paper>
					{contestId && (
						<>
							<Paper
								elevation={4}
								sx={{ padding: '1rem', marginBottom: '0.5rem' }}
							>
								<Typography
									textAlign="center"
									variant="h5"
									marginBottom="0.5rem"
								>
									Contest Tasks
								</Typography>
								<Stack>
									{tasks?.map((task, index) => (
										<Paper
											elevation={4}
											sx={{ padding: '1rem', marginBottom: '0.5rem' }}
											key={task}
										>
											<Typography>
												<span style={{ fontWeight: 700 }}>
													#{index + 1} task:{' '}
												</span>
												{tasks}
											</Typography>
										</Paper>
									))}
									{showNewTaskCard ? (
										<NewTaskCard contestId={contestId} handleSubmit={handleSubmit} />
									) : (
										<Paper elevation={4} sx={{ padding: '1rem' }}>
											<Button
												fullWidth
												variant="contained"
												sx = {{background: '#3c324e'}}
												onClick={() => setShowNewTaskCard(true)}
											>
												ADD NEW TASK
											</Button>
										</Paper>
									)}
								</Stack>
							</Paper>
							<Button 
								sx = {{background: '#3c324e'}}variant="contained" size="large"
								to="/contests"
								>DONE
							</Button>
						</>
					)}
				</Stack>
			</Container>
		</LocalizationProvider>
	)
}
