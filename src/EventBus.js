/**
 * 事件总线，使用键值对存储
 * @author zhengyixue
 */

class EventBus {
    constructor() {
        this.events = this.events || new Object();
    }
}

//构造函数需要存储event事件
//发布事件，参数是事件的type和需要传递的参数
EventBus.prototype.emit = function (type, ...args) {
    let e;
    e = this.events[type];
    // 查看这个type的event有多少个回调函数，如果有多个需要依次调用。
    if (Array.isArray(e)) {
        for (let i = 0; i < e.length; i++) {
            e[i].apply(this, args);
        }
    } else {
        // e.apply(this, args);
    }
};

//监听函数，参数是事件type和触发时需要执行的回调函数
EventBus.prototype.addListener = function (type, fun) {
    const e = this.events[type];
    let currentIndex = -1
    if (!e) {   //如果从未注册过监听函数，则将函数放入数组存入对应的键名下
        this.events[type] = [fun];
        currentIndex = 0
    } else {  //如果注册过，则直接放入
        e.push(fun);
        //获取当前组件监听函数，在观察函数数组中的索引，移除监听时使用
        currentIndex = this.events[type].length - 1
    }
    return { type, index: currentIndex }
};


//移除监听
EventBus.prototype.remove = function (subscribe) {
    let { type, index } = subscribe
    let e;
    e = this.events[type];
    // 查看这个type的event有多少个回调函数，如果有多个需要依次调用。
    if (Array.isArray(e)) {
        //监听的函数为空，则空处理
        if (e.length === 0) {
            return
        } else if (e.length === 1) {
            //只有一个监听的函数，则直接移除监听
            e.splice(0, 1)
        } else {
            //如果同一个key存在多个监听函数，只移除当前组件监听函数
            for (let i = 0; i < e.length; i++) {
                if (index > 0 && i === index) {
                    e.splice(index, 1)
                }
            }
        }
    } else {
        e = []
    }
};

//移除所有监听
EventBus.prototype.removeAll = function () {
    //移除所有监听函数
    if (this.events.length > 0) {
        this.events.length = 0;
    }
};

const eventBus = new EventBus();
export default eventBus;
