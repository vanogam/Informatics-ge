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
	Box
} from '@mui/material'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import { useRef, useState } from 'react'
import axios from 'axios'
import NewTaskCard from '../Components/NewTaskCard'
import { useParams } from 'react-router-dom'
import { NavLink } from 'react-router-dom'
import FormGroup from '@mui/material/FormGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox'
export default function EditContest() {
	const contestId = useParams()
	const [contestName, setContestName] = useState(null)
    const [saved, setSaved] = useState(null)
	const [value, setValue] = useState(dayjs('2014-08-18T21:11:54'))
	const [durationType, setDurationType] = useState('Minutes')
	const [tasks, setTasks] = useState([])
	const nameRef = useRef(null)
	const durationRef = useRef(null)
	const [showNewTaskCard, setShowNewTaskCard] = useState(false)
	const durationTypes = ['Hours', 'Minutes']
	const[archive, setArchive] = useState(false)
	const[autoArchive, setAutoArchive] = useState(false)
	console.log(archive)
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
            contestId: parseInt(contestId.contest_id),
			upsolving: archive, 
			upsolvingAfterFinish : autoArchive
		}
		params["durationInSeconds"] = params["durationInSeconds"].toString()
		console.log(params)
		setContestName(nameRef?.current.value)
		axios
			.post(`${process.env.REACT_APP_HOST}/create-contest`, params)
			.then((res) =>{setSaved(true)})
	}

	const handleSubmit = (title) => {
		setTasks((prevState) => [...prevState, title])
		setShowNewTaskCard(false)
	}
	const handleChange = (event) => {
		setArchive(event.target.checked);
	  };
	const handleChange2 = (event) => {
		setAutoArchive(event.target.checked);
	};
	return (
		<LocalizationProvider dateAdapter={AdapterDayjs}>
			<Container maxWidth="xs">
				<Stack gap="1rem" marginTop="2rem">
					<Paper elevation={4} sx={{ padding: '1rem' }}>
						{!saved ?(
							<>
								<Typography variant="h5" align="center" pb="1rem">
									კონტესტის რედაქტირება: {contestId.contest_id}
								</Typography>
								<Stack gap="1rem" maxWidth="25rem" mx="auto">
									<TextField
										label="Contest Name"
										inputRef={nameRef}
										variant="outlined"
									
									/>
									<Box sx ={{	display: 'flex',
					flexDirection: 'row'}}>
									<FormGroup>
										<FormControlLabel control={<Checkbox
										 checked={archive}
										 onChange={handleChange}
										inputProps={{ 'aria-label': 'controlled' }}
										
										   />} label="დაარქივება" />
										</FormGroup>
										<FormGroup>
										<FormControlLabel control={<Checkbox checked={autoArchive}
										 onChange={handleChange2}
										inputProps={{ 'aria-label': 'controlled' }} />} label="ავტომატური დაარქივება" />
										</FormGroup>
									</Box>
									  
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
										რედაქტირება
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
					{saved && (
						<>
							<Paper
								elevation={4}
								sx={{ padding: '1rem', marginBottom: '0.5rem' }}
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
										<NewTaskCard contestId={parseInt(contestId.contest_id)} handleSubmit={handleSubmit} />
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