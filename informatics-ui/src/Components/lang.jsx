

const langDictionary = {
	'ka' : {
		// ავტორიზაცია და რეგისტრაცია
		'registerSuccess': 'რეგისტრაცია წარმატებით დასრულდა',
		'loginSuccess': 'ავტორიზაცია წარმატებულია',
		'incorrectCredentials': 'არასწორი სახელი ან პაროლი',
		'ERROR_CODE_incorrectCredentials': 'არასწორი სახელი ან პაროლი',
		'ERROR_CODE_loggedInWithDifferentUser': 'სხვა მომხმარებელი უკვე ავტორიზებულია',

		// კონტესტები
		'name': 'სახელი',
		'duration': 'ხანგრძლივობა',
		'hourShort': 'სთ',
		'minuteShort': 'წთ',
		'DATEFORMAT_Minutes': 'წუთი',
		'DATEFORMAT_Hours': 'საათი',
		'scoringType': 'გაშვების პრიორიტეტი',
		'SCORING_TYPE_BEST_SUBMISSION': 'საუკეთესო გაშვება',
		'SCORING_TYPE_LAST_SUBMISSION': 'ბოლო გაშვება',
		'upsolvingAfterFinished': 'ამოცანების აფსოლვინგში დამატება შეჯიბრების დასრულების შემდეგ',
		'startDate': 'დაწყების დრო',

		'unexpectedException': 'გაუთვალისწინებელი შეცდომა',
		'insufficientPrivileges': 'ამ ქმედების განხორციელების უფლება არ გაქვთ!',
		'pleaseLogin': 'გთხოვთ, გაიაროთ ავტორიზაცია',
		'missingRequiredFields': 'გთხოვთ, შეავსოთ აუცილებელი ველები',
		'startDateAndDurationError': 'დაწყების თარიღი და ხანგრძლივობიდან ერთ-ერთი არ შეიძლება იყოს ცარიელი',

		'addProblem': 'ამოცანის დამატება',
		'addContest': 'დაამატე შეჯიბრება',
		'editContest': '{0}-ის რედაქტირება',

		'title': 'სათაური',
		'kaStatement': 'პირობა',
		'taskCode': 'ამოცანის უნიკალური კოდი',
		'taskType': 'ამოცანის ტიპი',
		'taskScoreType': 'ქულების დაწერის წესის ტიპი',
		'taskSuccessfullyAdded': 'ამოცანა წარმატებით დაემატა',
		'taskSuccessfullyEdited': 'ამოცანა წარმატებით განახლდა',
		'testcases': 'ტესტები',
		'addSingleTestcase': 'დაამატე ტესტი',
		'addMultipleTestcases': 'დაამატე რამდენიმე ტესტი',
		'uploadedFile': 'ფაილი',
		'uploadInputFile': 'შემავალი ფაილი',
		'uploadOutputFile': 'გამომავალი ფაილი',
		'uploadArchive': 'ატვირთე არქივი',
		'addedTestcases': 'დაემატა ტესტები',
		'failedToAddTestcases': 'ვერ დაემატა',
		'deleteSelected': 'მონიშნულის წაშლა',
		'downloadTestcases': 'ტესტების ჩამოტვირთვა',
		'testcaseDeleted': 'ტესტი წაშლილია',
		'add': 'დამატება',
		'save': 'შენახვა',

		'taskScoreParameter': 'ქულების დაწერის წესი',
		'timeLimitMillis': 'დროის ლიმიტი (მწ)',
		'memoryLimitMB': 'მეხსიერების ლიმიტი (MB)',
		'inputTemplate': 'შემავალი ფაილის სახელის შაბლონი',
		'outputTemplate': 'გამომავალი ფაილის სახელის შაბლონი',

		'TASK_SCORE_TYPE_SUM': 'ტესტების ჯამი',
		'TASK_SCORE_TYPE_GROUP_MIN': 'ტესტთა ჯგუფების ჯამი',

		'TASK_TYPE_BATCH': 'სტანდარტული',
		'edit': 'რედაქტირება',


		'editor': 'რედაქტორი',
		'statement': 'პირობა',
		'markdownPlaceholder': 'აკრიფეთ ტექსტი...',
	}
}

const getMessage = (lang, message, ...args) => {
	if (!langDictionary[lang][message]) {
		return message;
	}
	let localizedMessage = langDictionary[lang][message];
	args.forEach((arg, index) => {
		localizedMessage = localizedMessage.replace(`{${index}}`, arg);
	});
	return localizedMessage;
};
export default getMessage;