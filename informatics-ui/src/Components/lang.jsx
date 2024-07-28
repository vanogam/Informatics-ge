

const langDictionary = {
	'ka' : {
		'duration': 'ხანგრძლივობა',
		'hourShort': 'სთ',
		'minuteShort': 'წთ',
		'DATEFORMAT_Minutes': 'წუთი',
		'DATEFORMAT_Hours': 'საათი',
		'addProblem': 'ახალი ამოცანის დამატება',
		'addContest': 'დაამატე შეჯიბრება',
		'unexpectedException': 'გაუთვალისწინებელი შეცდომა',
		'insufficientPrivileges': 'ამ ქმედების განხორციელების უფლება არ გაქვთ!',
		'pleaseLogin': 'გთხოვთ, გაიაროთ ავტორიზაცია',
		'missingRequiredFields': 'გთხოვთ, შეავსოთ აუცილებელი ველები',

		'title': 'სათაური',
		'taskCode': 'ამოცანის უნიკალური კოდი',
		'taskType': 'ამოცანის ტიპი',
		'taskScoreType': 'ქულების დაწერის წესის ტიპი',
		'taskScoreParameter': 'ქულების დაწერის წესი',
		'timeLimitMillis': 'დროის ლიმიტი (მწ)',
		'memoryLimitMB': 'მეხსიერების ლიმიტი (MB)',
		'inputTemplate': 'შემავალი ფაილის სახელის შაბლონი',
		'outputTemplate': 'გამომავალი ფაილის სახელის შაბლონი',

		'ERROR_CODE_incorrectCredentials': 'არასწორი სახელი ან პაროლი',
		'ERROR_CODE_loggedInWithDifferentUser': 'სხვა მომხმარებელი უკვე ავტორიზებულია',

		'TASK_SCORE_TYPE_SUM': 'ტესტების ჯამი',
		'TASK_SCORE_TYPE_GROUP_MIN': 'ტესტთა ჯგუფების ჯამი',

		'TASK_TYPE_BATCH': 'სტანდარტული',
	}
}

const getMessage = (lang, message) => {
	if (!langDictionary[lang][message]) {
		return message;
	}
	return langDictionary[lang][message];
}
export default getMessage;