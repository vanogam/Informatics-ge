export const SCORE_RGB = {
	low: '244, 67, 54', // red
	partial: '255, 193, 7', // yellow
	full: '76, 175, 80', // green
}

export const getScoreCategory = (score) => {
	if (score === null || score === undefined) {
		return null
	}
	if (score < 1) {
		return 'low'
	}
	if (score <= 99) {
		return 'partial'
	}
	return 'full'
}

export const getScoreCellBackground = (score) => {
	const category = getScoreCategory(score)
	if (!category) {
		return 'inherit'
	}
	switch (category) {
		case 'low':
			return '#ffebee'
		case 'partial':
			return '#fff9c4'
		case 'full':
			return '#e8f5e9'
		default:
			return 'inherit'
	}
}

export const getScoreRowBackground = (score, transparency = 0.2) => {
	const category = getScoreCategory(score)
	if (!category) {
		return 'transparent'
	}
	const rgb = SCORE_RGB[category]
	return `rgba(${rgb}, ${transparency})`
}

export const getScoreRowHoverBackground = (
	score,
	baseTransparency = 0.2,
	hoverTransparency = 0.3
) => {
	const base = getScoreRowBackground(score, baseTransparency)
	if (base === 'transparent') {
		return 'rgba(0, 0, 0, 0.04)'
	}
	return base.replace(`, ${baseTransparency})`, `, ${hoverTransparency})`)
}

