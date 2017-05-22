'use strict';

describe('edit feedback detail controller [EditCtrl]', function() {

	var scope, adminController, model, account;
	var deferred;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_, _Account_) {
		scope = $rootScope.$new();

		deferred = $q.defer();

    model = _Model_;
    spyOn(model, 'getRegisteredUsers').and.returnValue(deferred.promise);
    spyOn(model, 'getAllFeedbackCycles').and.returnValue(deferred.promise);

    account = _Account_;
    spyOn(account, 'updateUser').and.returnValue(deferred.promise);

    adminController = $controller('AdminCtrl',{$scope: scope });
	}));

    describe('has valid initialisation values', function() {

    	it('should define functions', function() {
            expect(angular.isFunction(adminController.clearSelectedCycle)).toBe(true);
            expect(angular.isFunction(adminController.clearSelectedUser)).toBe(true);
            expect(angular.isFunction(adminController.getFeedbackCycle)).toBe(true);
            expect(angular.isFunction(adminController.initialiseNewCycle)).toBe(true);
            expect(angular.isFunction(adminController.updateFeedbackCycle)).toBe(true);
            expect(angular.isFunction(adminController.createNewFeedbackCycle)).toBe(true);
            expect(angular.isFunction(adminController.updateUser)).toBe(true);
            expect(angular.isFunction(adminController.removeQuestion)).toBe(true);
            expect(angular.isFunction(adminController.addQuestion)).toBe(true);
            expect(angular.isFunction(adminController.openStart)).toBe(true);
            expect(angular.isFunction(adminController.openEnd)).toBe(true);
            expect(angular.isFunction(adminController.setSelectedCycleDetails)).toBe(true);
            expect(angular.isFunction(adminController.updateQuestionResponse)).toBe(true);
    	});

    	it('for global controller variables', function() {
            expect(adminController).toBeDefined();
            expect(adminController.registeredUsers).toEqual([]);
            expect(adminController.reviewCycles).toEqual([]);
            expect(adminController.selectedUser).not.toBeDefined();
            expect(adminController.selectedCycle).not.toBeDefined();
            expect(adminController.selectedCycleDetails).not.toBeDefined();
            expect(adminController.flattenedQuestionResponse).toEqual({});
            expect(adminController.error).not.toBeDefined();
            expect(adminController.startPopup.opened).toBe(false);
            expect(adminController.endPopup.opened).toBe(false);
    	});

    	it('and calls the necessary services to pre-populate the model', function(){
            expect(model.getRegisteredUsers).toHaveBeenCalled();
            expect(model.getAllFeedbackCycles).toHaveBeenCalled();
    	});

	});

	describe('has simple state manipulation functions to', function() {

    it('open the start date picker popup', function() {
      expect(adminController.startPopup.opened).toBe(false)
      adminController.openStart();
      expect(adminController.startPopup.opened).toBe(true)
    });

    it('open the end date picker popup', function() {
      expect(adminController.endPopup.opened).toBe(false)
      adminController.openEnd();
      expect(adminController.endPopup.opened).toBe(true)
    });

    it('remove a question from selected details', function() {
      adminController.selectedCycleDetails = {"questions": ["one", "two", "three", "four", "five"]};
      adminController.flattenedQuestionResponse = {0: "one", 1: "two", 2: "three", 3: "four", 4: "five"};

      adminController.removeQuestion(2);
      expect(adminController.selectedCycleDetails.questions).toEqual(["one", "two", "four", "five"]);
      expect(adminController.flattenedQuestionResponse).toEqual({0: "one", 1: "two", 2: "four", 3: "five", 4: undefined});

      adminController.removeQuestion(3);
      expect(adminController.selectedCycleDetails.questions).toEqual(["one", "two", "four"]);
      expect(adminController.flattenedQuestionResponse).toEqual({0: "one", 1: "two", 2: "four", 3: undefined, 4: undefined});

      adminController.removeQuestion(0);
      expect(adminController.selectedCycleDetails.questions).toEqual(["two", "four"]);
      expect(adminController.flattenedQuestionResponse).toEqual({0: "two", 1: "four", 2: undefined, 3: undefined, 4: undefined});

    });

    it('add a blank question to the selected cycle question array', function() {
      adminController.selectedCycleDetails = { "questions": []};
      adminController.addQuestion();
      expect(adminController.selectedCycleDetails.questions).toEqual([{ "responseOptions": [], "format": "RADIO"}])
      expect(adminController.flattenedQuestionResponse[0]).toEqual([]);

      adminController.addQuestion();
      expect(adminController.selectedCycleDetails.questions).toEqual([{ "responseOptions": [], "format": "RADIO"},{ "responseOptions": [], "format": "RADIO"}])
      expect(adminController.flattenedQuestionResponse[1]).toEqual([]);
    });

    it('should not add a question when the selected cycle details are undefined', function() {
      adminController.selectedCycleDetails = undefined;
      adminController.addQuestion();
      expect(adminController.selectedCycleDetails).toBeUndefined();

      adminController.selectedCycleDetails = {"questions": undefined};
      adminController.addQuestion();
      expect(adminController.selectedCycleDetails.questions).toBeUndefined();
    });

    it('remove a question from the selected cycle question array', function() {
      adminController.selectedCycleDetails = { "questions" : ["zero", "one", "two", "three", "four"]}
      adminController.removeQuestion(4);
      expect(adminController.selectedCycleDetails.questions).toEqual(["zero", "one", "two", "three"]);

      adminController.removeQuestion(0);
      expect(adminController.selectedCycleDetails.questions).toEqual(["one", "two", "three"]);

      adminController.removeQuestion(1);
      expect(adminController.selectedCycleDetails.questions).toEqual(["one", "three"]);
    });

    it('clear the selected user', function() {
      adminController.selectedUser = {"some":"thing"}
      adminController.clearSelectedUser();
      expect(adminController.selectedUser).toBeUndefined()
    });

    it('clear the selected cycle', function() {
      adminController.selectedCycle = {"some":"thing"};
      adminController.selectedCycleDetails = {"some":"one"};
      adminController.flattenedQuestionResponse = {"some":"things"};
      adminController.clearSelectedCycle();

      expect(adminController.selectedCycle).toBeUndefined()
      expect(adminController.selectedCycleDetails).toBeUndefined()
      expect(adminController.flattenedQuestionResponse).toEqual({});
    });

    it('should initialise a new cycle for creation', function() {
      expect(adminController.selectedCycleDetails).toBeUndefined();
      adminController.initialiseNewCycle();
      expect(adminController.selectedCycleDetails).toEqual({
        "active": false,
        "hasForcedSharing": false,
        "hasOptionalSharing": true,
        "isThreeSixtyReview": false,
        "questions": [ {"responseOptions":[], "format": "RADIO"},
                       {"responseOptions":[], "format": "RADIO"},
                       {"responseOptions":[], "format": "RADIO"},
                       {"responseOptions":[], "format": "RADIO"},
                       {"responseOptions":[], "format": "RADIO"}]
      });
      expect(adminController.flattenedQuestionResponse).toEqual({"0":[],"1":[],"2":[],"3":[],"4":[]});
    });

    it('should update the selected cycle details and set date functions for the date pickers', function() {
      var input = {"some": "thing", "startDate":"2017-05-22T00:00:00.000+1000", "endDate": "2017-05-26T00:00:00.000+1000"};
      expect(adminController.selectedCycleDetails).toBeUndefined();

      adminController.setSelectedCycleDetails(input);

      expect(adminController.selectedCycleDetails.some).toEqual("thing");
      expect(adminController.selectedCycleDetails.startDate).toEqual(new Date("2017-05-22T00:00:00.000+1000"))
      expect(adminController.selectedCycleDetails.endDate).toEqual(new Date("2017-05-26T00:00:00.000+1000"))
    });

    it('should update the question response options when changed', function() {
      var question = {"responseOptions": []};

      adminController.updateQuestionResponse(question, "1\n3\n5")

      expect(question).toEqual({"responseOptions":['1','3','5']});
    })
	});

  describe('wraps server call functions', function() {

    it('should route the update to the correct update function', function() {
      spyOn(adminController, 'createNewFeedbackCycle');
      spyOn(adminController, 'updateFeedbackCycle');
      var input = {"some": "thing"};

      adminController.saveChanges(input, true);
      expect(adminController.createNewFeedbackCycle).toHaveBeenCalledWith(input);

      adminController.saveChanges(input, false);
      expect(adminController.updateFeedbackCycle).toHaveBeenCalledWith(input);
    });
  });
});
