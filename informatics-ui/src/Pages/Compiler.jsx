import React from 'react'
import Box from '@mui/material/Box'
import Editor from 'react-simple-code-editor'
import { highlight, languages } from 'prismjs/components/prism-core'
import 'prismjs/components/prism-clike'
import 'prismjs/components/prism-c'
import 'prismjs/components/prism-cpp'
import 'prismjs/components/prism-javascript'
import 'prismjs/themes/prism.css' //Example style, you can use another
import '../styles/numbers.css'
import { Button } from '@mui/material'
import TextField from '@mui/material/TextField'
import { AxiosContext } from '../utils/axiosInstance'

const rightColumnStyle = {
	display: 'flex',
	flexDirection: 'column',
	'& .MuiTextField-root': { m: 1, width: '100%', maxWidth: '100%', boxSizing: 'border-box' },
	paddingTop: '5%',
	paddingLeft: '8px',
	paddingRight: '24px',
	flex: '1 1 35%',
	minWidth: 280,
	color: 'purple',
	fontWeight: 'bold',
	fontSize: '20',
}
const highlightWithLineNumbers = (input, grammar, language) =>
	highlight(input, grammar, language)
		.split('\n')
		.map((line, i) => `<span class='editorLineNumber'>${i + 1}</span>${line}`)
		.join('\n')

export default function Compiler() {
	const axiosInstance = React.useContext(AxiosContext)

	const [code, setCode] = React.useState(
		`#include <iostream>\nusing namespace std;\nint main()\n{\ncout << "Hello, World!";\nreturn 0; \n}\n`
	)
	const [input, setInput] = React.useState('')
	const [output, setOutput] = React.useState('')
	const [status, setStatus] = React.useState(null)
	const [time, setTime] = React.useState(null)
	const [memory, setMemory] = React.useState(null)
	const [message, setMessage] = React.useState('')
	const [isRunning, setIsRunning] = React.useState(false)
	const pollRef = React.useRef(null)
	const editorWrapperRef = React.useRef(null)

	React.useEffect(() => {
		const wrapper = editorWrapperRef.current
		if (!wrapper) return
		const textarea = wrapper.querySelector('#codeArea')
		if (!textarea) return
		const scrollToCursor = () => {
			const lineIndex = textarea.value.substring(0, textarea.selectionStart).split('\n').length - 1
			const computed = getComputedStyle(textarea)
			const lineHeightStr = computed.lineHeight
			const lineHeight = lineHeightStr === 'normal'
				? parseFloat(computed.fontSize) * 1.2
				: parseFloat(lineHeightStr)
			const cursorTop = lineIndex * lineHeight
			const cursorBottom = cursorTop + lineHeight
			if (cursorTop < wrapper.scrollTop) {
				wrapper.scrollTop = cursorTop
			} else if (cursorBottom > wrapper.scrollTop + wrapper.clientHeight) {
				wrapper.scrollTop = cursorBottom - wrapper.clientHeight
			}
		}
		textarea.addEventListener('keyup', scrollToCursor)
		textarea.addEventListener('click', scrollToCursor)
		return () => {
			textarea.removeEventListener('keyup', scrollToCursor)
			textarea.removeEventListener('click', scrollToCursor)
		}
	}, [])

	const stopPolling = () => {
		if (pollRef.current) {
			clearInterval(pollRef.current)
			pollRef.current = null
		}
	}

	React.useEffect(() => {
		return () => stopPolling()
	}, [])

	const handleRun = async () => {
		if (isRunning) {
			return
		}

		try {
			setIsRunning(true)
			setStatus('IN_QUEUE')
			setOutput('')
			setTime(null)
			setMemory(null)
			setMessage('')

			const response = await axiosInstance.post('/custom-test', {
				code,
				language: 'CPP',
				input,
			})

			const key = response.data.key
			if (!key) {
				setIsRunning(false)
				return
			}

			pollRef.current = setInterval(async () => {
				try {
					const statusResponse = await axiosInstance.get(`/custom-test/${key}`, {
						ignoreErrors: true,
					})
					const data = statusResponse.data
					if (!data) {
						return
					}
					setStatus(data.status)
					if (data.message) {
						setMessage(data.message)
					}
					if (data.timeMillis != null) {
						setTime(data.timeMillis)
					}
					if (data.memoryKb != null) {
						setMemory(data.memoryKb)
					}
					if (data.outcome != null) {
						setOutput(data.outcome)
					}

					if (
						['FINISHED', 'FAILED', 'COMPILATION_ERROR', 'SYSTEM_ERROR'].includes(
							data.status
						)
					) {
						stopPolling()
						setIsRunning(false)
					}
				} catch (e) {
					// Ignore transient polling errors
				}
			}, 1000)
		} catch (e) {
			setIsRunning(false)
		}
	}

	return (
		<Box
			sx={{
				display: 'flex',
				flexDirection: 'row',
				width: '100%',
				gap: 0,
				boxSizing: 'border-box',
			}}
		>
			<Box
				sx={{
					paddingTop: '50px',
					paddingLeft: '16px',
					paddingRight: '8px',
					display: 'flex',
					flexDirection: 'column',
					flex: '1 1 55%',
					minWidth: 280,
					fontSize: '20',
				}}
			>
			<p style = {{color:'#452c54', fontWeight: 'bold'}}>შეიყვანე კოდი: </p>
			<div ref={editorWrapperRef} style={{ minHeight: '50vh', maxHeight: '50vh', overflow: 'auto', width: '100%' }}>
				<Editor
					value={code}
					onValueChange={(newCode) => setCode(newCode)}
					highlight={(code) =>
						highlightWithLineNumbers(code, languages.cpp, 'cpp')
					}
					className="editor"
					textareaId="codeArea"
					style={{
						width: '100%',
						minHeight: '50vh',
						fontFamily: '"Fira code", "Fira Mono", monospace',
						fontSize: 12,
					}}
				/>
			</div>
				<Button
					sx={{
						marginInline: '2px',
						alignSelf: 'right',
						marginLeft: '25%',
						width: '40%',
						marginTop: '5%',
						background: '#3c324e',
					}}
					onClick={handleRun}
					variant="contained"
				>
					{isRunning ? 'მუშავდება...' : 'გაშვება'}
				</Button>
			</Box>

			<Box sx={rightColumnStyle} component="form">
				<p style = {{color:'#452c54', fontWeight: 'bold'}}>შემავალი მონაცემები: </p>
				<TextField
					id="outlined-multiline-static"
					multiline
					rows={3}
					value={input}
					onChange={(e) => setInput(e.target.value)}
				></TextField>

				<Button
					sx={{
						marginInline: '2px',
						alignSelf: 'right',
						width: '60%',
						// marginBottom: '20%',
						marginLeft: '20%',
						background: '#3c324e',
					}}
					variant="contained"
				>
					ფაილის ატვირთვა
				</Button>

				<p style = {{color:'#452c54', fontWeight: 'bold'}}>გამომავალი მონაცემები: </p>
				<TextField
					sx={{
						'& .MuiInputBase-input': { color: 'black' },
					}}
					InputProps={{ readOnly: true }}
					id="outlined-multiline-static"
					multiline
					rows={3}
					value={output}
				></TextField>
				{status && (
					<div style={{ marginTop: '8px', fontSize: 14, color: '#452c54' }}>
						<div>
							<p>სტატუსი: {status}</p>
							<p>{time != null && ` დრო: ${time} ms`}</p>
							<p>{memory != null && ` მეხსიერება: ${memory} KB`}</p>
						</div>
						{message && (
							<pre
								style={{
									marginTop: '4px',
									whiteSpace: 'pre-wrap',
									fontFamily: '"Fira code", "Fira Mono", monospace',
									fontSize: 12,
								}}
							>
								{message}
							</pre>
						)}
					</div>
				)}
			</Box>
		</Box>
	)
}
