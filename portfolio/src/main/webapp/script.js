// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random personal fact to the page.
 */
function addRandomFact() {
  const facts = [
    'I have 2 siblings.',
    'My favorite color is blue.',
    'I enjoy outdoor activities.',
    'I skipped a grade.',
    'I was born in Massachusetts.',
  ];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/**
 * Cycles backwards to the previous background image.
 */
function prevBackground() {
  changeBackground(-1);
}

/**
 * Cycles forwards to the next background image.
 */
function nextBackground() {
  changeBackground(1);
}

let currentBackgroundIndex = 0;

// List of background images and their color themes.
const backgroundThemes = [
  {
    img: 'forest.jpg',
    primaryColor: '#523029',
    headingBgColor: 'rgba(160,82,45,.1)',
    highlightBgColor: 'rgba(34,139,34,.3)',
    buttonBgColor: 'rgba(160,82,45,.7)',
  }, {
    img: 'ocean.jpg',
    primaryColor: 'navy',
    headingBgColor: 'rgba(65,105,225,.1)',
    highlightBgColor: 'rgba(0,191,255,.2)',
    buttonBgColor: 'rgba(135,206,235,.9)',
  }, {
    img: 'mountain.jpg',
    primaryColor: 'midnightblue',
    headingBgColor: 'rgba(0,128,128,.15)',
    highlightBgColor: 'rgba(25,25,112,.2)',
    buttonBgColor: 'rgba(175, 238, 238,.9)'
  }, {
    img: 'balloons.jpg',
    primaryColor: 'indianred',
    headingBgColor: 'rgba(255,215,0,.2)',
    highlightBgColor: 'rgba(255,165,0,.5)',
    buttonBgColor: 'rgba(255,69,0,.7)',
  }, {
    img: 'iceberg.jpg',
    primaryColor: 'darkslategray',
    headingBgColor: 'rgba(72,61,139,.2)',
    highlightBgColor: 'rgba(0,50,128,.4)',
    buttonBgColor: 'rgba(90,150,150,.9)',
  }, {
    img: 'grass.jpg',
    primaryColor: 'green',
    headingBgColor: 'rgba(173,255,47,.2)',
    highlightBgColor: 'rgba(0,255,0,.4)',
    buttonBgColor: 'rgba(127,255,0,.9)',
  },
];

/**
 * Advances the background image by a given offset from the current image.
 */
function changeBackground(offset) {
  // Update the index of the current background image.
  currentBackgroundIndex = (currentBackgroundIndex + offset) % backgroundThemes.length;

  // Wrap around to the last image if the index is negative.
  if (currentBackgroundIndex < 0) {
    currentBackgroundIndex += backgroundThemes.length;
  }

  const newBackgroundTheme = backgroundThemes[currentBackgroundIndex];

  // Update the page with the new background image.
  const html = document.documentElement;
  html.style.backgroundImage = `url(images/${newBackgroundTheme.img})`;

  // Update the color theme based on the new background image.
  updateColorTheme(newBackgroundTheme);
}

/**
 * Updates the CSS properties for the color theme.
 */
function updateColorTheme(backgroundTheme) {
  document.body.style.setProperty('--primary-theme-color', backgroundTheme.primaryColor);
  document.body.style.setProperty('--heading-bg-color', backgroundTheme.headingBgColor);
  document.body.style.setProperty('--highlight-bg-color', backgroundTheme.highlightBgColor);
  document.body.style.setProperty('--button-bg-color', backgroundTheme.buttonBgColor);
}

/**
 * Fetches the list of comments from the server and adds them to the DOM.
 */
async function getComments() {
  const maxComments = document.getElementById('max-comments').value;
  const sortOption = document.getElementById('sort-option').value;

  const response = await fetch(`/data?max-comments=${maxComments}&sort-option=${sortOption}`);
  const comments = await response.json();

  const commentsContainer = document.getElementById('comments-container');
  commentsContainer.innerHTML = '';

  // Add a message if there are no comments.
  if (comments.length === 0) {
    const commentElement = document.createElement('div');
    const pElement = createTextElement(commentElement, 'p', 'No comments here...');
    pElement.id = 'empty-comment';
    commentsContainer.appendChild(commentElement);
  } else {
    // Create <h4> and <p> elements for each comment's author and text.
    comments.forEach((comment) => {
      const commentElement = document.createElement('div');
      
      const pElement = createTextElement(commentElement, 'p', comment.formattedDate);
      pElement.classList.add('comment-date');
      createTextElement(commentElement, 'h4', comment.author);
      createTextElement(commentElement, 'p', comment.commentText);

      commentsContainer.appendChild(commentElement);
    });
  }
}

/**
 * Creates an html element containing the specified text and
 * inserts it as the last child of a given parent element.
 */
function createTextElement(parentElement, htmlTag, innerText) {
  const element = document.createElement(htmlTag);
  element.innerText = innerText;
  parentElement.appendChild(element);
  return element;
}

/**
 * Posts a new comment to the server and updates the list of comments.
 */
async function postComment(event) {
  // Prevent the default action of reloading the page to prevent the background theme from resetting.
  event.preventDefault();

  const authorInput = document.getElementById('author-input');
  const commentInput = document.getElementById('comment-input');

  const response = await fetch(`/data?author=${authorInput.value}&comment=${commentInput.value}`, {
    method: 'POST'
  });

  // Clear out the form input.
  authorInput.value = commentInput.value = '';
  getComments();
}

/**
 * Deletes all comments from the server and removes them from the page.
 */
async function deleteAllComments() {
  const response = await fetch('/delete-data', {
    method: 'POST'
  });
  
  getComments();
}