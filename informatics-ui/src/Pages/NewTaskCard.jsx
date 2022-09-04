import { Button, Paper, Stack, TextField, Typography } from '@mui/material'
import { useRef, useState } from 'react'
import NewTestCaseCard from './NewTestCaseCard'
import axios from 'axios'
export default function NewTaskCard({ contestId, handleSubmit }) {
	console.log("CONTESTID", contestId)
	const codeRef = useRef(null)
	const titleENref = useRef(null)
	const titleKAref = useRef(null)
	const taskTypeRef = useRef(null)
	const taskScoreTypeRef = useRef(null)
	const taskScoreParameterRef = useRef(null)
	const timeLimitMillisRef = useRef(null)
	const memoryLimitMBref = useRef(null)
	const inputTemplateRef = useRef(null)
	const outputTemplateRef = useRef(null)
	const enStatementRef = useRef(null)
	const kaStatementRef = useRef(null)

	const [showNewTestCaseCard, setShowNewTestCaseCard] = useState(false)
	const [testCases, setTestCases] = useState([])
	const [taskId, setTaskId] = useState("")
	const handleTestCaseSubmit = (params) => {
		setTestCases((prevState) => [...prevState, params])
		setShowNewTestCaseCard(false)
	}

	const handleNewTask = () => {
		const code = codeRef?.current.value.toString()
		const enStatement = enStatementRef?.current.files[0]
		const kaStatement = kaStatementRef?.current.files[0]
		const enTitle = titleENref?.current.value
		const kaTitle = titleKAref?.current.value
		const taskType = taskTypeRef?.current.value
		const taskScoreType = taskScoreTypeRef?.current.value
		const taskScoreParameter = taskScoreParameterRef?.current.value.toString()
		const timeLimitMillis = timeLimitMillisRef?.current.value
		const memoryLimitMB = memoryLimitMBref?.current.value
		const inputTemplate = inputTemplateRef?.current.value
		const outputTemplate = outputTemplateRef?.current.value
		// if (
		// 	!code ||
		// 	!enStatement ||
		// 	!kaStatement ||
		// 	!enTitle ||
		// 	!kaTitle ||
		// 	!taskType ||
		// 	!taskScoreType ||
		// 	!taskScoreParameter ||
		// 	!timeLimitMillis ||
		// 	!memoryLimitMB ||
		// 	!inputTemplate ||
		// 	!outputTemplate
		// )
		// 	return
		// Axios To ADD NEW TASK
		// Axios To UPLOAD STATEMENT
		// then for loop over testCases and for each one Axios to addTestCases
		const params = {
				"contestId": contestId,
				"code": code.toString(),
				"title" : {
					"KA": kaTitle.toString(),
					"EN": enTitle.toString()
				},
				"taskType": taskType.toString(),
				"taskScoreType": taskScoreType.toString(),
				"taskScoreParameter": parseFloat(taskScoreParameter),
				"timeLimitMillis": parseInt(timeLimitMillis),
				"memoryLimitMB":parseInt(memoryLimitMB),
				"inputTemplate":inputTemplate.toString(),
				"outputTemplate": outputTemplate.toString()
		}
		console.log({
			code,
			enStatement,
			kaStatement,
			enTitle,
			kaTitle,
			taskType,
			taskScoreType,
			taskScoreParameter,
			timeLimitMillis,
			memoryLimitMB,
			inputTemplate,
			outputTemplate,
		})
		axios
		.post('http://localhost:8080/save-task', params)
		.then((res) => {setTaskId(res.data.taskDTO.id);
			console.log("NOW UPLOAD STATEMENT")
			var bodyFormData = new FormData();
			bodyFormData.append("taskId", res.data.taskDTO.id)
			bodyFormData.append("language", "KA")
			bodyFormData.append("statement", kaStatement)
			console.log("BodyFromData", bodyFormData)
			axios({
				method: "post",
				url: 'http://localhost:8080/upload-statement',
				data: bodyFormData,
				headers: { "Content-Type": 'multipart/form-data; boundary=<calculated when request is sent></calculated>'},
			  })
				.then(function (response) {
				  //handle success
				console.log(testCases)
				for(const testCase of testCases){
					var bodyFormData = new FormData();
					bodyFormData.append("taskId", res.data.taskDTO.id)
					bodyFormData.append("file",testCase["testCaseFile"])
					console.log("BodyFromData", bodyFormData)
					console.log("TestCases", testCases)
					console.log("TestCase", testCase)
					axios({
						method: "post",
						url: 'http://localhost:8080/add-testcases',
						data: bodyFormData,
						headers: { "Content-Type": 'multipart/form-data; boundary=<calculated when request is sent></calculated>'},
						})
						.then(function (response) {
							//handle success
							console.log(response);
						})
						.catch(function (response) {
							//handle error
							console.log(response);
						});
						console.log(response);
				}
				})
				.catch(function (response) {
				  //handle error
				  console.log(response);
				});})
		handleSubmit(enTitle)
	}
	return (
		<Paper elevation={4} sx={{ padding: '1rem' }}>
			<Typography align="center" variant="h5" mb="1rem">
				New Task
			</Typography>
			<Stack gap="1rem" maxWidth="25rem" mx="auto" mb="1rem">
				<TextField 	size = "small" label="Code" inputRef={codeRef} variant="outlined" />
				<Stack flexDirection="row" gap="1rem">
					<Button fullWidth variant="contained" component="label">
						EN Statement
						<input ref={enStatementRef} type="file" hidden />
					</Button>
					<Button fullWidth variant="contained" component="label">
						KA Statement
						<input ref={kaStatementRef} type="file" hidden />
					</Button>
				</Stack>
				<Stack flexDirection="row" gap="1rem">
					<TextField
						label="Title (en)"
						inputRef={titleENref}
						variant="outlined"
						size = "small"
					/>
					<TextField
						label="Title (ka)"
						inputRef={titleKAref}
						variant="outlined"
						size = "small"
					/>
				</Stack>
				{/* <TextField type="file" variant="outlined" /> */}
				<TextField size = "small" label="taskType" inputRef={taskTypeRef} variant="outlined" />
				<TextField
					label="taskScoreType"
					inputRef={taskScoreTypeRef}
					variant="outlined"
					size = "small"
				/>
				<TextField
					label="taskScoreParameter"
					inputRef={taskScoreParameterRef}
					variant="outlined"
					size = "small"
				/>
				<TextField
					label="timeLimitMillis"
					inputRef={timeLimitMillisRef}
					variant="outlined"
					size = "small"
				/>
				<TextField
					label="memoryLimitMB"
					inputRef={memoryLimitMBref}
					variant="outlined"
					size = "small"
				/>
				<TextField
					label="inputTemplate"
					inputRef={inputTemplateRef}
					variant="outlined"
					size = "small"
				/>
				<TextField
					label="outputTemplate"
					inputRef={outputTemplateRef}
					variant="outlined"
					size = "small"
				/>
			</Stack>
			{testCases?.map((testCase, index) => (
				<Paper
					elevation={4}
					sx={{ padding: '1rem', marginBottom: '0.5rem' }}
					key={`${testCase?.testCaseFile?.name}@${testCase.selectedLanguage}`}
				>
					<Typography>
						<span style={{ fontWeight: 700 }}>#{index + 1} test: </span>
						{testCase?.testCaseFile?.name}
					</Typography>
				</Paper>
			))}
			{showNewTestCaseCard ? (
				<NewTestCaseCard taskId = {taskId} handleTestCaseSubmit={handleTestCaseSubmit} />
			) : (
				<Paper elevation={4} sx={{ padding: '1rem', marginBottom: '0.5rem' }}>
					<Button
						fullWidth
						variant="contained"
						sx ={{background: '#3c324e'}}
						onClick={() => setShowNewTestCaseCard(true)}
					>
						ADD NEW TEST CASES
					</Button>
				</Paper>
			)}
			<Button
				fullWidth
				variant="contained"
				size="large"
				sx={{ marginTop: '1rem' , background: '#3c324e',}}
				onClick={() => {
					handleNewTask()}}
			>
				Add Task
			</Button>
		</Paper>
	)
}
