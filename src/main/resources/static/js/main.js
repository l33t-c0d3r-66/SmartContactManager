function toggleSideBar(){
    if($(".sidebar").is(":visible")) {
        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");
        $(".fa-bars").css("display","block");
        $(".fa-bars").css("padding-top","10px");
    } else {
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
        $(".fa-bars").css("display","none");
    }
}

const search = () => {
    let query = $("#search-input").val();
    if(query=='') {

    } else {
        let url = `http://localhost:8080/search/${query}`;
        fetch(url).then(response=>{
            return response.json();
        })
            .then((data)=> {
                let text = `<div class='list-group'>`
                data.forEach(contact => {
                    text+=`<a href='/user/${contact.contactId}/contact' class='list-group-item list-group-item-action'>${contact.name}</a>`;
                }) ;
                text+=`</div>`
                $(".search-result").html(text);
                $(".search-result").show();
            });

    }
}

function paymentStart() {
    let amount = $("#payment").val();
    if (amount == '' || amount == null) {
        return;
    } else {
        $.ajax (
            {
                url : '/user/create-order',
                data:JSON.stringify({amount:amount, info: 'order_request'}),
                contentType: 'application/json',
                type: 'POST',
                dataType: 'json',
                success: function(response) {
                    if (response.status == "created") {
                        let options = {
                            /**
                             * TODO
                             * Place your key here
                             *
                             */
                            key: 'place_key here',
                            amount: response.amount,
                            currency: response.currency,
                            name: 'Smart Contact Manager',
                            description: 'Donation',
                            image: '',
                            order_id: response.id,
                            handler: function (response) {
                                console.log(response.razorpay_payment_id);
                                console.log(response.razorpay_order_id);
                                console.log(response.razorpay_signature);
                                console.log("Payment Successful!");
                                //alert("Congrats! Payment Successful");
                                updatePaymentInformation(response.razorpay_payment_id,response.razorpay_order_id,'paid');
                            },
                            prefill: {
                                "name": "",
                                "email": "",
                                "contact": ""
                            },
                            notes: {
                                "address": "l33t-c0d3t-66"
                            }
                        };
                        var razorpay = new Razorpay(options);
                        razorpay.on('payment.failed', function (response) {
                            console.log(response.error.code);
                            console.log(response.error.description);
                            console.log(response.error.source);
                            console.log(response.error.step);
                            console.log(response.error.reason);
                            console.log(response.error.metadata.order_id);
                            console.log(response.error.metadata.payment_id);
                            swal("Failed!","Amount is Required","error");
                        });
                        razorpay.open();
                    }
                },
                error: function(error) {

                }
            }
        )

    }

}

function updatePaymentInformation(payment_id, order_id, status) {
    $.ajax (
        {
            url: '/user/update-order',
            data: JSON.stringify({paymentId: payment_id, orderId: order_id, status: status}),
            contentType: 'application/json',
            type: 'POST',
            dataType: 'json',
            success: function (response) {
                swal("Good Job","Congrats! Your Payment is Sucessful","sucess");
            },
            error: function(error) {
                swal("Failed","Your Payment is Successful but we didn't get on server","error");
            }
        }
    )
}