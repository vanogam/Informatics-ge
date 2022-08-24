import * as React from 'react'
import Card from '@mui/material/Card'
import CardActions from '@mui/material/CardActions'
import CardContent from '@mui/material/CardContent'
import CardMedia from '@mui/material/CardMedia'
import Button from '@mui/material/Button'
import Typography from '@mui/material/Typography'
import news1 from '../Components/news1.jpg'
import news2 from '../Components/news2.jpg'
import news3 from '../Components/news3.jpg'
import { Box } from '@mui/material'

export default function Main() {
	return (
		<Box>
			<Box sx={{ marginLeft: '15%', marginTop: '5%' }}>
				<Button
					sx={{
						marginInline: '2px',
						alignSelf: 'flex-end',
						color: '#4a366c',
						fontWeight: 'bold',
						fontFamily: '"Lucida Console", "Courier New", monospace',
						fontSize: '1.5rem',
					}}
				>
					სიახლეები
				</Button>
			</Box>
			<hr
				style={{
					color: '#a48fca',
					backgroundColor: '#2c1c48',
					height: 5,
					width: '70%',
				}}
			/>

			<Card
				sx={{
					marginTop: '3%',
					marginBottom: '0',
					marginLeft: '15%',
					marginRight: '10%',
					maxWidth: '40%',
					maxHeight: '10%',
				}}
			>
				<CardMedia
					width="20"
					component="img"
					height="200"
					image={news1}
					alt="green iguana"
				/>
				<CardContent>
					<Typography gutterBottom variant="h5" component="div">
						დათო ჩეჩელაშვილი Amazon-ში იწყებს მუშაობას
					</Typography>
					<Typography variant="body2" color="text.secondary">
						თავისუფალი უნივერსიტეტის MACS[E]-ის სკოლის მათემატიკისა და
						კომპიუტერული მეცნიერების მიმართულების მეოთხე კურსის სტუდენტი, დათო
						ჩეჩელაშვილი ამაზონის ბერლინის ოფისს Software Development Engineer-ის
						პოზიციაზე შეუერთდება...
					</Typography>
				</CardContent>
				<CardActions>
					<Button size="small">ვრცლად</Button>
				</CardActions>
			</Card>

			<Card
				sx={{
					marginTop: '5%',
					marginBottom: '5%',
					marginLeft: '15%',
					marginRight: '10%',
					maxWidth: '40%',
					maxHeight: '10%',
				}}
			>
				<CardMedia
					width="20"
					component="img"
					height="200"
					image={news2}
					alt="green iguana"
				/>
				<CardContent>
					<Typography gutterBottom variant="h5" component="div">
						საბა ცერცვაძე Google-ის გუნდს შეუერთდება
					</Typography>
					<Typography variant="body2" color="text.secondary">
						თავისუფალი უნივერსიტეტის MACS[E]-ის სკოლის კომპიუტერული
						მეცნიერებების პროგრამის სტუდენტი, საბა ცერცვაძე სექტემბრიდან
						Google-ის ვარშავის ოფისს Google Cloud Engine Managed
						Infrastructure-ის (GCE Minfra) გუნდში შეუერთდება Software
						Engineer-ის პოზიციაზე...
					</Typography>
				</CardContent>
				<CardActions>
					<Button size="small ">ვრცლად</Button>
				</CardActions>
			</Card>

			<Card
				sx={{
					marginTop: '5%',
					marginBottom: '5%',
					marginLeft: '15%',
					marginRight: '10%',
					maxWidth: '40%',
					maxHeight: '10%',
				}}
			>
				<CardMedia
					width="20"
					component="img"
					height="200"
					image={news3}
					alt="green iguana"
				/>
				<CardContent>
					<Typography gutterBottom variant="h5" component="div">
						სტეფანე გურგენიძე მუშაობას Microsoft-დან Amazon-ში აგრძელებს
					</Typography>
					<Typography variant="body2" color="text.secondary">
						თავისუფალი უნივერსიტეტის MACS[E]-ის სკოლის კომპიუტერული
						მეცნიერებების პროგრამის სტუდენტი, სტეფანე გურგენიძე მუშაობას
						Amazon-ის ბარსელონას ოფისში იწყებს Software Development Engineer-ის
						პოზიციაზე...
					</Typography>
				</CardContent>
				<CardActions>
					<Button size="small">ვრცლად</Button>
				</CardActions>
			</Card>
		</Box>
	)
}
