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
import NewTaskCard from '../Components/NewTaskCard'
import { NavLink } from 'react-router-dom'
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
			.post(`${process.env.REACT_APP_HOST}/create-contest`, params)
			.then((res) => {setContestId(res.data.contest.id); 	axios.post(`${process.env.REACT_APP_HOST}/contest/${res.data.contest.id}/register`,{})})
	

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
								<Typography variant="h5" align="center" pb="1rem">
									ახალი კონტესტი
								</Typography>
								<Stack gap="1rem" maxWidth="25rem" mx="auto">
									<TextField
										label="სახელი"
										inputRef={nameRef}
										variant="outlined"
									/>
									<DateTimePicker
										label="დაწყების დრო"
										value={value}
										onChange={setValue}
										renderInput={(params) => (
											<TextField variant="outlined" {...params} />
										)}
									/>
									<Stack direction="row" gap="1rem">
										<TextField
											label="ხანგრძლივობა (წთ)"
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
										დაამატე კონტესტი
									</Button>
								</Stack>
							</>
						) : (
							<>
								<Typography variant="h5" align="center">
									{contestName}
								</Typography>
							</>
						)}
					</Paper>
					{contestId && (
						<>
							<Paper
								elevation={4}
								sx={{  padding: '1rem', marginBottom: '0.5rem' }}
							>
								<Typography
									textAlign="center"
									variant="h6"
									marginBottom="0.5rem"
								>
									 ამოცანები
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
												ახალი ამოცანის დამატება
											</Button>
										</Paper>
									)}
								</Stack>
							</Paper>
						

						<Button
							sx = {{background: '#3c324e'}}variant="contained" size="large"
							component={NavLink}
							to="/contests"
						>
							დასრულება
						</Button>
						</>
					)}
				</Stack>
			</Container>
		</LocalizationProvider>
	)
}
