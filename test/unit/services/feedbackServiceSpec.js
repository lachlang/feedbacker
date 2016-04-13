'use strict';

describe('feedback service [Feedback]', function() {
	
	var feedback, $httpBackend;

    beforeEach(module('feedbacker'));
    
    beforeEach(inject(function(_Feedback_, _$httpBackend_) {
    	feedback = _Feedback_;
    	$httpBackend = _$httpBackend_;
    }));
    
    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
      });
    
    it('can get an instantce of itself', function(){
    	expect(feedback).toBeDefined();
    });
    
    xit('has defined functions', function() {
    	expect(angular.isFunction(feedback.getPendingFeedbackActions)).toBe(true);
    });

    xdescribe("calls the appropriate server api", function() {
    	
    	it('to retrieve pending feedback list', function() {
    		var result, promise = feedback.getPendingFeedbackActions();

    		$httpBackend.expectGET('/api/feedback/pending').respond(200, { "data" : [{"id": 10, thing:"stuff" }, {"id":11, thing:"more"}]});
    		
    		// set the response value
    		promise.then(function(data) {
    			result = data;
    		});
    		expect(result).toBeUndefined(); // it really should at this point
    		$httpBackend.flush();

    		expect(result).toBeDefined();
    		expect(result).toEqual([{"id": 10, thing:"stuff" }, {"id":11, thing:"more"}]);
    	});

    });

});