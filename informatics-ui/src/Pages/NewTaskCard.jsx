import { Button, Paper, Stack, TextField, Typography } from '@mui/material'
import { useRef, useState } from 'react'
import NewTestCaseCard from './NewTestCaseCard'

export default function NewTaskCard({ handleSubmit }) {
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
	const handleTestCaseSubmit = (params) => {
		setTestCases((prevState) => [...prevState, params])
		setShowNewTestCaseCard(false)
	}

	const handleNewTask = () => {
		const code = codeRef?.current.value
		const enStatement = enStatementRef?.current.files[0]
		const kaStatement = kaStatementRef?.current.files[0]
		const enTitle = titleENref?.current.value
		const kaTitle = titleKAref?.current.value
		const taskType = taskTypeRef?.current.value
		const taskScoreType = taskScoreTypeRef?.current.value
		const taskScoreParameter = taskScoreParameterRef?.current.value
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

		handleSubmit(enTitle)
	}
	return (
		<Paper elevation={4} sx={{ padding: '1rem' }}>
			<Typography align="center" variant="h4" mb="1rem">
				New Task
			</Typography>
			<Stack gap="1rem" maxWidth="25rem" mx="auto" mb="1rem">
				<TextField label="Code" inputRef={codeRef} variant="outlined" />
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
					/>
					<TextField
						label="Title (ka)"
						inputRef={titleKAref}
						variant="outlined"
					/>
				</Stack>
				{/* <TextField type="file" variant="outlined" /> */}
				<TextField label="taskType" inputRef={taskTypeRef} variant="outlined" />
				<TextField
					label="taskScoreType"
					inputRef={taskScoreTypeRef}
					variant="outlined"
				/>
				<TextField
					label="taskScoreParameter"
					inputRef={taskScoreParameterRef}
					variant="outlined"
				/>
				<TextField
					label="timeLimitMillis"
					inputRef={timeLimitMillisRef}
					variant="outlined"
				/>
				<TextField
					label="memoryLimitMB"
					inputRef={memoryLimitMBref}
					variant="outlined"
				/>
				<TextField
					label="inputTemplate"
					inputRef={inputTemplateRef}
					variant="outlined"
				/>
				<TextField
					label="outputTemplate"
					inputRef={outputTemplateRef}
					variant="outlined"
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
				<NewTestCaseCard handleTestCaseSubmit={handleTestCaseSubmit} />
			) : (
				<Paper elevation={4} sx={{ padding: '1rem', marginBottom: '0.5rem' }}>
					<Button
						fullWidth
						variant="contained"
						onClick={() => setShowNewTestCaseCard(true)}
					>
						ADD NEW TEST CASE
					</Button>
				</Paper>
			)}
			<Button
				fullWidth
				variant="contained"
				color="success"
				size="large"
				sx={{ marginTop: '1rem' }}
				onClick={handleNewTask}
			>
				Add Task
			</Button>
		</Paper>
	)
}
